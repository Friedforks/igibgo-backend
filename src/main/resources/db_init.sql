/* full text search config */
CREATE EXTENSION IF NOT EXISTS pg_trgm;
CREATE EXTENSION IF NOT EXISTS zhparser;
CREATE TEXT SEARCH CONFIGURATION chinese (PARSER = zhparser);
ALTER TEXT SEARCH CONFIGURATION chinese
    ADD MAPPING FOR n,v,a,i,e,l,t WITH simple;

create table f_user
(
    user_id         bigserial primary key not null,
    username        text                  not null,
    avatar_url      text                  not null,
    is_teacher      boolean               not null default false,
    email           text                  not null,
    subscribe_count int                   not null default 0,
    password        text                  not null,
    token           text
);

create table collection
(
    collection_id   bigserial primary key not null,
    author          int references f_user (user_id) on delete cascade,
    collection_name text                  not null
);

create table note
(
    note_id       text      not null primary key,
    author        int       not null references f_user (user_id) on delete cascade,
    like_count    bigint    not null default 0,
    save_count    bigint    not null default 0,
    view_count    bigint    not null default 0,
    reply_count   bigint    not null default 0,
    collection_id int       not null references collection (collection_id) on delete cascade,
    note_url      text      not null,
    upload_date   timestamp not null,
    title         text      not null,
    title_tsv     tsvector  generated always as ( to_tsvector('chinese',title) ) stored
);


-- index tsvector
create index note_title_tsv_idx on note using gin(title_tsv);


create table note_tag
(
    note_tag_id bigserial primary key not null,
    note_id     text                  not null references note (note_id) on delete cascade,
    tag_text    text                  not null
);

create table note_reply
(
    note_reply_id bigserial not null primary key,
    note_id       text      not null references note (note_id) on delete cascade,
    reply_content text      not null,
    reply_date    timestamp not null,
    author        int       not null references f_user (user_id) on delete cascade
);

create table bookmark
(
    bookmark_id   bigserial primary key not null,
    user_id       bigint references f_user (user_id) on delete cascade,
    bookmark_name text                  not null
);
create table note_bookmark
(
    note_bookmark_id bigserial not null,
    bookmark_id      bigint references bookmark (bookmark_id) on delete cascade,
    note_id          text      not null references note (note_id) on delete cascade
);

create table note_like
(
    note_like_id bigserial primary key not null,
    note_id      text                  not null references note (note_id) on delete cascade,
    user_id      bigint                not null references f_user (user_id) on delete cascade
);

create table note_view
(
    note_view_id bigserial primary key not null,
    note_id      text                  not null references note (note_id) on delete cascade,
    user_id      bigint                not null references f_user (user_id) on delete cascade
);

create table video
(
    video_id        text      not null primary key,
    author          int       not null references f_user (user_id) on delete cascade,
    like_count      bigint    not null default 0,
    save_count      bigint    not null default 0,
    view_count      bigint    not null default 0,
    reply_count     bigint    not null default 0,
    collection_id   int       not null references collection (collection_id) on delete cascade,
    video_url       text      not null,
    video_cover_url text      not null,
    upload_date     timestamp not null,
    title           text      not null,
    title_tsv       tsvector generated always as ( to_tsvector('chinese', title)) stored

);

-- add index for title_tsv
create index video_title_tsv_idx on video using gin(title_tsv);

create table video_tag
(
    video_tag_id bigserial primary key not null,
    video_id     text                  not null references video (video_id) on delete cascade,
    tag_text     text                  not null
);

create table video_reply
(
    video_reply_id bigserial not null primary key,
    video_id       text      not null references video (video_id) on delete cascade,
    reply_content  text      not null,
    reply_date     timestamp not null,
    author         int       not null references f_user (user_id) on delete cascade
);


create table video_bookmark
(
    video_bookmark_id bigserial not null,
    bookmark_id       bigint references bookmark (bookmark_id) on delete cascade,
    video_id          text      not null references video (video_id) on delete cascade
);

create table video_view
(
    video_view_id bigserial primary key not null,
    video_id      text                  not null references video (video_id) on delete cascade,
    user_id       bigint                not null references f_user (user_id) on delete cascade
);

create table video_like
(
    video_like_id bigserial primary key not null,
    video_id      text                  not null references video (video_id) on delete cascade,
    user_id       bigint                not null references f_user (user_id) on delete cascade
);

create table post
(
    post_id      text      not null primary key,
    author       int       not null references f_user (user_id) on delete cascade,
    like_count   int       not null default 0,
    view_count   int       not null default 0,
    upload_date  timestamp not null,
    post_content text      not null,
    post_type    int       not null,
    title        text      not null
);

create table post_tag
(
    post_tag_id bigserial not null primary key,
    post_id     text      not null references post (post_id) on delete cascade,
    tag_text    text      not null
);

create table post_reply
(
    post_reply_id bigserial not null primary key,
    post_id       text      not null references post (post_id) on delete cascade,
    reply_content text      not null,
    author        int       not null references f_user (user_id) on delete cascade
);
