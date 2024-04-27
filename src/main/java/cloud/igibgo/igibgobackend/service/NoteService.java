package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.*;
import cloud.igibgo.igibgobackend.entity.Collection;
import cloud.igibgo.igibgobackend.mapper.*;
import cloud.igibgo.igibgobackend.util.UploadUtil;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.its.asn1.HashedData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Slf4j
@Service
public class NoteService {
    @Resource
    private NoteMapper noteMapper;

    @Transactional
    public Page<Note> getNotesInOrder(PageRequest pageRequest) {
        return noteMapper.findAll(pageRequest);
    }

    /**
     * O(NlogM) where N is the number of notes and M is the number of tags
     *
     * @param tags list of tags
     * @return set of notes that have at least one of the tags
     */
    public Set<Note> getNotesByTags(List<String> tags) {
        Set<Note> notes = new HashSet<>();
        for (String tag : tags) {
            List<Note> notesLinkedWithTag = noteMapper.findAllByTag(tag);
            notes.addAll(notesLinkedWithTag);
        }
        return notes;
    }

    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private CollectionMapper collectionMapper;

    @Resource
    private NoteTagMapper noteTagMapper;

    @Resource
    private UploadUtil uploadUtil;

    // TODO Optimize this method to make save to COS and db in parallel
    public void uploadNote(MultipartFile note, Long authorId, Long collectionId, String title,List<String> tags) throws IOException {
        Optional<FUser> author = fUserMapper.findById(authorId);
        // Check 1: if the author  exist
        if (author.isPresent()) {
            // Check 2: if collection exist (if collection id is not null)
            /* it exists only when
             * 1. collection id is null
             * 2. collection id is not null and collection exists
             */
            Optional<Collection> collection = collectionMapper.findById(collectionId);
            if (collection.isEmpty()) {
                throw new IllegalArgumentException("Collection not found");
            }
            // Check 3: file type
            String originalFilename = note.getOriginalFilename();
            assert originalFilename != null;
            String suffix = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
            if (!suffix.equalsIgnoreCase("pdf")) {// only support pdf
                throw new IllegalArgumentException("File type not supported, please upload notes in PDF, MD or DOCX format");
            }
            // 1. Generate new file name
            String generatedNoteId=UUID.randomUUID().toString();
            String newFilename = generatedNoteId+ "." + suffix;
            // 2. create the note folder if empty
            Path tmpNoteFile = Files.createTempFile(null, newFilename);
            note.transferTo(tmpNoteFile);
            // 3. upload note to COS
            String url = uploadUtil.upload(tmpNoteFile.toFile(), newFilename, "note/");
            // 4. fetch collection (if collection id is not null)
            Collection c = collection.get();
            Note noteInstance = new Note();
            noteInstance.noteId= generatedNoteId;
            noteInstance.author = author.get();
            noteInstance.collection = c;
            noteInstance.title = title;
            noteInstance.noteUrl = url;
            // 4. save note to db
            noteMapper.save(noteInstance);
            // 5. Save tags
            List<NoteTag> noteTags = new ArrayList<>();
            for (String tag : tags) {
                NoteTag noteTag = new NoteTag();
                noteTag.note = noteInstance;
                noteTag.tagText = tag;
                noteTagMapper.save(noteTag);
            }
            noteTagMapper.saveAll(noteTags);
        } else {
            throw new IllegalArgumentException("Author not found");
        }
    }


    public void likeNote(String noteId) {
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        // if note exists
        if (noteOptional.isPresent()) {
            Note note = noteOptional.get();
            note.likeCount++;
            noteMapper.save(note);
        } else {
            throw new IllegalArgumentException("Note not found with the given note id");
        }
    }

    public Note getNoteByNoteId(String noteId) {
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        // Check 1: if the note exists
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found with the given note id");
        }
        // 1. get the note
        Note note = noteOptional.get();
        // 2. increment the view count
        note.viewCount++;
        // 3. save the note to db
        noteMapper.save(note);
        return note;
    }

    public List<Note> getNotesByTitle(String title){
        return noteMapper.findAllByTitle(title);
    }

    public void bookmarkNote(String noteId, Long userId) {
        // Check 1: if the user exists
        if (fUserMapper.existsById(userId)) {
            // Check 2: if the note exists
            Optional<Note> note = noteMapper.findById(noteId);
            if (note.isPresent()) {
                // 1. get the note
                Note noteInstance = note.get();
                noteInstance.saveCount++;
                // 2. save the note to db
                noteMapper.save(noteInstance);
            } else {
                throw new IllegalArgumentException("Note not found");
            }
        } else {
            throw new IllegalArgumentException("User not found");
        }
    }



    public List<String> getAllTags(){
        // fetch all distinct tag content from db
        return noteMapper.findDistinctTags();
    }

    public void deleteNote(Long author, String noteId) {
        // Check 1: if author exist
        Optional<FUser> authorOptional = fUserMapper.findById(author);
        if (authorOptional.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        // Check 2: if note exists
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found");
        }
        // Check 3: if the author is the author of the note
        Note note = noteOptional.get();
        if (!note.author.userId.equals(author)) {
            throw new IllegalArgumentException("You are not the author of the note");
        }

        // 1. delete the note from COS
        // 1.1 convert public access url to file path in COS
        // E.g. public accessurl= https://igibgo-1305786880.cos.ap-guangzhou.myqcloud.com/note/1b3e7b7b.pdf
        // and file path in COS= note/1b3e7b7b.pdf
        String filePath = "note/" + note.noteUrl.substring(note.noteUrl.indexOf("/"));
        uploadUtil.deleteObject(filePath);
        // 2. delete the note from db
        noteMapper.deleteById(noteId);
    }

    @Resource
    private NoteReplyMapper noteReplyMapper;

    public void replyNote(String noteId, String replyContent, Long authorId) {
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        if (noteOptional.isEmpty()) {
            throw new IllegalArgumentException("Note not found");
        }
        Optional<FUser> authorOptional = fUserMapper.findById(authorId);
        if (authorOptional.isEmpty()) {
            throw new IllegalArgumentException("Author not found");
        }
        Note note = noteOptional.get();
        NoteReply reply = new NoteReply();
        reply.replyContent = replyContent;
        reply.author = authorOptional.get();
        reply.note = note;
        noteReplyMapper.save(reply);
    }

    public void deleteReply(Long replyId, Long authorId) {
        // Check 1: if the reply exist
        Optional<NoteReply> noteReplyOptional = noteReplyMapper.findById(replyId);
        if (noteReplyOptional.isPresent()) {
            // Check 2: if the author exist
            Optional<FUser> userOptional = fUserMapper.findById(authorId);
            if (userOptional.isPresent()) {
                // 1. delete the reply
                noteReplyMapper.deleteById(replyId);
            } else {
                throw new IllegalArgumentException("Author does not exist");
            }
        } else {
            throw new IllegalArgumentException("Reply does not exist");
        }
    }

}
