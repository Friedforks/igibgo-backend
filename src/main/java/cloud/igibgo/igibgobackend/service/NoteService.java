package cloud.igibgo.igibgobackend.service;

import cloud.igibgo.igibgobackend.entity.APIResponse;
import cloud.igibgo.igibgobackend.entity.Collection;
import cloud.igibgo.igibgobackend.entity.Note;
import cloud.igibgo.igibgobackend.mapper.CollectionMapper;
import cloud.igibgo.igibgobackend.mapper.FUserMapper;
import cloud.igibgo.igibgobackend.mapper.NoteMapper;
import cloud.igibgo.igibgobackend.util.UploadUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
public class NoteService {
    @Resource
    private NoteMapper noteMapper;

    public Page<Note> getNotesInOrder(PageRequest pageRequest){
        return noteMapper.findAll(pageRequest);
    }

    /**
     * O(NlogM) where N is the number of notes and M is the number of tags
     *
     * @param tags list of tags
     * @return list of notes that have at least one of the tags
     */
    public List<Note> getNoteByTags(List<String> tags) {
        Set<Note> notes = new HashSet<>();
        for (String tag : tags) {
            notes.addAll(noteMapper.findAllByTag(tag));
        }
        return notes.stream().toList();
    }

    /**
     * get note with replies and tags
     *
     * @param noteId note id
     * @return note with replies and tags
     */
    public Note getNoteWithReplies(String noteId) {
        return noteMapper.findByNoteIdWithRepliesAndTags(noteId);
    }

    @Resource
    private FUserMapper fUserMapper;

    @Resource
    private CollectionMapper collectionMapper;

    public void uploadNote(MultipartFile note, Long author, Long collectionId, String title) throws IOException {
        // Check 1: if the author  exist
        if (fUserMapper.existsById(author)) {
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
            if (!suffix.equalsIgnoreCase("pdf") && !suffix.equalsIgnoreCase("md") && !suffix.equalsIgnoreCase("docx")) {
                throw new IllegalArgumentException("File type not supported, please upload notes in PDF, MD or DOCX format");
            }
            // 1. convert note to file
            File noteFile = new File("notes/" + originalFilename);
            note.transferTo(noteFile);
            // 2. upload note to COS
            String newFilename = UUID.randomUUID() + "." + suffix;
            String url = UploadUtil.upload(noteFile, newFilename, "note/");
            // 3. fetch collection (if collection id is not null)
            Collection c = collection.get();
            Note noteInstance = new Note();
            noteInstance.author = author;
            noteInstance.collection = c;
            noteInstance.title = title;
            noteInstance.noteUrl = url;
            // 4. save note to db
            noteMapper.save(noteInstance);
        } else {
            throw new IllegalArgumentException("Author not found");
        }
    }


    public void likeNote(String noteId) {
        Optional<Note> noteOptional = noteMapper.findById(noteId);
        // if note exists
        if (noteMapper.existsById(noteId) && noteOptional.isPresent()) {
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
        Note note=noteOptional.get();
        // 2. increment the view count
        note.viewCount++;
        noteMapper.save(note);
        return note;
    }

    public void bookmarkNote(String noteId, Long userId) {
        // Check 1: if the user exists
        if (fUserMapper.existsById(userId)) {
            // Check 2: if the note exists
            if (noteMapper.existsById(noteId)) {
                // 1. get the note
                Optional<Note> note = noteMapper.findById(noteId);
                if(note.isPresent()){
                    Note noteInstance = note.get();
                    noteInstance.saveCount++;
                    noteMapper.save(noteInstance);
                }
                else{
                    throw new IllegalArgumentException("Note not found");
                }
            }
        }
    }
}
