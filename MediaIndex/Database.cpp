/*
<one line to give the program's name and a brief idea of what it does.>
Copyright (C) 2012  Benjamin Schmitz <vortex@wolpzone.de>

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/


#include "Database.h"
#include <iostream>
#include <QDebug>
#include <assert.h>

Database::Database()
{
    db = NULL;
    insertTrackStmt = NULL;
    updateTrackStmt = NULL;
    getLastModifiedStmt = NULL;
    beginStmt = NULL;
    commitStmt = NULL;
    dropUnmarkedStmt = NULL;
    clearMarkStmt = NULL;
    markStmt = NULL;
    addCoverStmt = NULL;
    checkCoverStmt = NULL;
    setCoverStmt = NULL;
    countStmt = NULL;
    coverCountStmt = NULL;
    missingCoverCountStmt = NULL;
}

Database::~Database()
{
    // free prepared statements and close database file
    if (insertTrackStmt) {
        sqlite3_finalize(insertTrackStmt);
        sqlite3_finalize(updateTrackStmt);
        sqlite3_finalize(getLastModifiedStmt);
        sqlite3_finalize(beginStmt);
        sqlite3_finalize(commitStmt);
        sqlite3_finalize(dropUnmarkedStmt);
        sqlite3_finalize(clearMarkStmt);
        sqlite3_finalize(markStmt);
        sqlite3_finalize(addCoverStmt);
        sqlite3_finalize(checkCoverStmt);
        sqlite3_finalize(setCoverStmt);
        sqlite3_finalize(countStmt);
        sqlite3_finalize(coverCountStmt);
        sqlite3_finalize(missingCoverCountStmt);
    }

    if (db) {
        sqlite3_close(db);
    }

    std::cout << std::endl << "Database closed." << std::endl;
}

bool Database::open(char* path)
{
    if (!checkReturn(sqlite3_open(path, &db))) {
        std::cout << "Error: could not open database file." << std::endl;
        return false;
    }

    std::cout << "Successfully opened database file: " << path << std::endl;
    return true;
}

void Database::prepare()
{
    // prepare statements that are used repeatedly
    //qDebug() << "SQLITE: prepare statements";
    bool success = true;
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "BEGIN",
                           -1,
                           &beginStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "COMMIT",
                           -1,
                           &commitStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "INSERT INTO tracks (title, artist, album, genre, track, year, path, lastmodified, mark) VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'), 1)",
                           -1,
                           &insertTrackStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "UPDATE tracks SET title=?, artist=?, album=?, genre=?, track=?, year=?, lastmodified=datetime('now'), mark=1, cover=NULL WHERE path=?", // unset cover on update
                           -1,
                           &updateTrackStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "SELECT id, strftime('%s', lastmodified) FROM tracks WHERE path=?",
                           -1,
                           &getLastModifiedStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "DELETE FROM tracks WHERE mark=0",
                           -1,
                           &dropUnmarkedStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "UPDATE tracks SET mark=0",
                           -1,
                           &clearMarkStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "UPDATE tracks SET mark=1 WHERE id=?",
                           -1,
                           &markStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "INSERT INTO covers (md5, data, length, mimetype) VALUES (?,?,?,?)",
                           -1,
                           &addCoverStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "SELECT md5 FROM covers WHERE md5=?",
                           -1,
                           &checkCoverStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "UPDATE tracks SET cover=? WHERE path=?",
                           -1,
                           &setCoverStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "SELECT COUNT(*) FROM tracks",
                           -1,
                           &countStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "SELECT COUNT(*) FROM covers",
                           -1,
                           &coverCountStmt,
                           NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                           "SELECT COUNT(*) FROM tracks WHERE cover IS NULL",
                           -1,
                           &missingCoverCountStmt,
                           NULL));
    if (!success) {
        std::cerr << "FATAL: Error preparing statements." << std::endl;
        exit(1);
    }
}

bool Database::create()
{
    sqlite3_stmt *createStmt;

    // create table for tracks
    sqlite3_prepare_v2(db,
                       "CREATE TABLE tracks (id INTEGER PRIMARY KEY AUTOINCREMENT,"
                       "title TEXT,"
                       "artist TEXT,"
                       "album TEXT,"
                       "genre TEXT,"
                       "track INTEGER,"
                       "year INTEGER,"
                       "cover TEXT,"
                       "path TEXT UNIQUE," // creates index on path
                       "lastmodified DATE,"
                       "mark INTEGER)",
                       -1,
                       &createStmt,
                       NULL);
    int ret = sqlite3_step(createStmt);

    if (!ret == SQLITE_DONE) {
        // create table failed
        std::cout << "Error: could not create track table: " << sqlite3_errmsg(db) << std::endl;
    }

    sqlite3_finalize(createStmt);

    // create table for cover art
    sqlite3_prepare_v2(db,
                       "CREATE TABLE covers (md5 TEXT PRIMARY KEY, data BLOB, length INTEGER, mimetype TEXT)",
                       -1,
                       &createStmt,
                       NULL);
    ret = sqlite3_step(createStmt);

    if (!ret == SQLITE_DONE) {
        // create table failed
        std::cout << "Error: could not create covers table: " << sqlite3_errmsg(db) << std::endl;
    }

    return (ret == SQLITE_DONE);
}

void Database::begin()
{
    if (!beginStmt) {
        prepare();
    }
    assert(beginStmt);

    sqlite3_step(beginStmt);
    //sqlite3_reset(beginStmt);
}

void Database::commit()
{
    if (!commitStmt) {
        prepare();
    }
    assert(commitStmt);

    sqlite3_step(commitStmt);
    //sqlite3_reset(commitStmt);
}

void Database::insertTrack(const char* title, const char* artist, const char* album, const char* genre, int track, int year, const char* path)
{
    if (!insertTrackStmt) {
        prepare();
    }
    assert(insertTrackStmt);

    // bind parameters to prepared statement
    sqlite3_bind_text(insertTrackStmt, 1, title, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(insertTrackStmt, 2, artist, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(insertTrackStmt, 3, album, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(insertTrackStmt, 4, genre, -1, SQLITE_TRANSIENT);
    sqlite3_bind_int(insertTrackStmt, 5, track);
    sqlite3_bind_int(insertTrackStmt, 6, year);
    sqlite3_bind_text(insertTrackStmt, 7, path, -1, SQLITE_TRANSIENT);

    int ret;
    if ((ret = sqlite3_step(insertTrackStmt)) != SQLITE_DONE) {
        std::cout << std::endl << "Warning: unable to add " << path << " to database. (" << ret << ")" << std::endl;
    }

    // reset statement for future use
    sqlite3_clear_bindings(insertTrackStmt);
    sqlite3_reset(insertTrackStmt);
}

void Database::updateTrack(const char* title, const char* artist, const char* album, const char* genre, int track, int year, const char* path)
{
    if (!updateTrackStmt) {
        prepare();
    }
    assert(updateTrackStmt);

    // bind parameters to prepared statement
    sqlite3_bind_text(updateTrackStmt, 1, title, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(updateTrackStmt, 2, artist, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(updateTrackStmt, 3, album, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(updateTrackStmt, 4, genre, -1, SQLITE_TRANSIENT);
    sqlite3_bind_int(updateTrackStmt, 5, track);
    sqlite3_bind_int(updateTrackStmt, 6, year);
    sqlite3_bind_text(updateTrackStmt, 7, path, -1, SQLITE_TRANSIENT);

    int ret;
    if ((ret = sqlite3_step(updateTrackStmt)) != SQLITE_DONE) {
        std::cout << std::endl << "Warning: unable to update " << path << " (" << ret << ")" << std::endl;
    }

    // reset statement for future use
    sqlite3_clear_bindings(updateTrackStmt);
    sqlite3_reset(updateTrackStmt);
}


sqlite3_int64 Database::getLastModified(const char* path)
{
    if (!getLastModifiedStmt) {
        prepare();
    }
    assert(getLastModifiedStmt);

    int ret;
    sqlite3_reset(getLastModifiedStmt);
    ret = sqlite3_bind_text(getLastModifiedStmt, 1, path, -1, SQLITE_TRANSIENT);
    //qDebug() << "SQLITE bind parameter: " << ret;

    ret = sqlite3_step(getLastModifiedStmt);
    //qDebug() << "SQLITE get lmod: " << path << " (" << ret << ")";
    if (ret != SQLITE_ROW) {
        // not found
        return -1;
    }
    sqlite3_int64 id = sqlite3_column_int64(getLastModifiedStmt, 0);
    sqlite_int64 value = sqlite3_column_int64(getLastModifiedStmt, 1);
    mark(id);

    // reset statement for future use
    sqlite3_clear_bindings(getLastModifiedStmt);

    //qDebug() << "SQLITE get lmod: " << path << ": " << value;

    return value;
}

void Database::mark(sqlite_int64 id)
{
    if (!markStmt) {
        prepare();
    }
    assert(markStmt);

    sqlite3_reset(markStmt);
    sqlite3_bind_int64(markStmt, 1, id);
    sqlite3_step(markStmt);
}


void Database::clearMarks()
{
    if (!clearMarkStmt) {
        prepare();
    }
    assert(clearMarkStmt);

    sqlite3_reset(clearMarkStmt);
    sqlite3_step(clearMarkStmt);
}

void Database::dropUnmarked()
{
    if (!dropUnmarkedStmt) {
        prepare();
    }
    assert(dropUnmarkedStmt);

    sqlite3_reset(dropUnmarkedStmt);
    sqlite3_step(dropUnmarkedStmt);
}

void Database::addCover(const char* md5, void* data, int length, const char* mimetype)
{
    if (!addCoverStmt) {
        prepare();
    }
    assert(addCoverStmt);

    sqlite3_reset(addCoverStmt);
    sqlite3_bind_text(addCoverStmt, 1, md5, -1, SQLITE_TRANSIENT);
    sqlite3_bind_blob(addCoverStmt, 2, data, length, SQLITE_TRANSIENT);
    sqlite3_bind_int(addCoverStmt, 3, length);
    sqlite3_bind_text(addCoverStmt, 4, mimetype, -1, SQLITE_TRANSIENT);
    sqlite3_step(addCoverStmt);
}

bool Database::hasCover(const char* md5)
{
    if (!checkCoverStmt) {
        prepare();
    }
    assert(checkCoverStmt);

    sqlite3_reset(checkCoverStmt);
    sqlite3_bind_text(checkCoverStmt, 1, md5, -1, SQLITE_TRANSIENT);
    return (sqlite3_step(checkCoverStmt) == SQLITE_ROW);
}

void Database::setCover(const char* md5, const char* path)
{
    if (!setCoverStmt) {
        prepare();
    }
    assert(setCoverStmt);

    sqlite3_reset(setCoverStmt);
    sqlite3_bind_text(setCoverStmt, 1, md5, -1, SQLITE_TRANSIENT);
    sqlite3_bind_text(setCoverStmt, 2, path, -1, SQLITE_TRANSIENT);
    sqlite3_step(setCoverStmt);
}


DbPathIterator Database::getAllPaths()
{
    sqlite3_stmt *getAllStmt;
    sqlite3_prepare_v2(db,
                       "SELECT path FROM tracks",
                       -1,
                       &getAllStmt,
                       NULL);

    return DbPathIterator(getAllStmt);
}

DbPathIterator Database::getMissingCovers()
{
    sqlite3_stmt *getMissingCoversStmt;
    sqlite3_prepare_v2(db,
                       "SELECT path FROM tracks WHERE cover IS NULL",
                       -1,
                       &getMissingCoversStmt,
                       NULL);

    return DbPathIterator(getMissingCoversStmt);
}


sqlite3_int64 Database::getTrackCount()
{
    if (!countStmt) {
        prepare();
    }
    assert(countStmt);

    sqlite3_reset(countStmt);
    sqlite3_step(countStmt);
    return sqlite3_column_int64(countStmt, 0);
}

sqlite3_int64 Database::getCoverCount()
{
    if (!coverCountStmt) {
        prepare();
    }
    assert(coverCountStmt);

    sqlite3_reset(coverCountStmt);
    sqlite3_step(coverCountStmt);
    return sqlite3_column_int64(coverCountStmt, 0);
}

sqlite3_int64 Database::getMissingCoverCount()
{
    if (!missingCoverCountStmt) {
        prepare();
    }
    assert(missingCoverCountStmt);

    sqlite3_reset(missingCoverCountStmt);
    sqlite3_step(missingCoverCountStmt);
    return sqlite3_column_int64(missingCoverCountStmt, 0);
}


DbCoverIterator Database::getAllCovers()
{
    sqlite3_stmt *getAllStmt;
    sqlite3_prepare_v2(db,
                       "SELECT md5, data, length, mimetype FROM covers",
                       -1,
                       &getAllStmt,
                       NULL);

    return DbCoverIterator(getAllStmt);
}


/*
 * Check return values for erros
 */
bool Database::checkReturn(int ret)
{
    //qDebug() << "SQLITE checkReturn: " << ret;
    if (!(ret == SQLITE_OK || ret == SQLITE_DONE)) {
        std::cerr << "SQLITE error ("<< ret << "): " << sqlite3_errmsg(db) << std::endl;
        return false;
    }
    return true;
}

