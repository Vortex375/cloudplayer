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
}

Database::~Database()
{
    // free prepared statements and close database file
    if (insertTrackStmt) {
        sqlite3_finalize(insertTrackStmt);
        sqlite3_finalize(beginStmt);
        sqlite3_finalize(commitStmt);
    }
    
    if (db) {
        sqlite3_close(db);
    }
}

bool Database::open(char* path)
{
    if (!checkReturn(sqlite3_open(path, &db))) {
        std::cout << "Error: could not open database file." << std::endl;
        return false;
    }

    return true;
}

void Database::prepare()
{
    // prepare statements that are used repeatedly
    bool success = true;
    success &= checkReturn(sqlite3_prepare_v2(db,
                       "INSERT INTO tracks (title, artist, album, genre, track, year, path, lastmodified) VALUES (?, ?, ?, ?, ?, ?, ?, datetime('now'))",
                       -1,
                       &insertTrackStmt,
                       NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                       "UPDATE tracks SET title=?, artist=?, album=?, genre=?, track=?, year=?, lastmodified=datetime('now') WHERE path=?",
                       -1,
                       &updateTrackStmt,
                       NULL));
    success &= checkReturn(sqlite3_prepare_v2(db,
                       "SELECT lastmodified FROM tracks WHERE path=?",
                       -1,
                       &getLastModifiedStmt,
                       NULL));
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
    if (!success) {
        std::cerr << "FATAL: Error preparing statements." << std::endl;
        exit(1);
    }
}

bool Database::create()
{
    sqlite3_stmt *createStmt;
    sqlite3_prepare_v2(db,
                       "CREATE TABLE tracks (id integer PRIMARY KEY AUTOINCREMENT,"
                                            "title varchar(200),"
                                            "artist varchar(200),"
                                            "album varchar(200),"
                                            "genre varchar(200),"
                                            "track integer,"
                                            "year integer,"
                                            "path varchar(800),"
                                            "lastmodified date)",
                       -1,
                       &createStmt,
                       NULL);

    int ret = sqlite3_step(createStmt);

    if (!ret == SQLITE_DONE) {
        // create table failed
        std::cout << "Error: could not create tables: " << sqlite3_errmsg(db) << std::endl;
    }

    sqlite3_finalize(createStmt);
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
    
    sqlite3_bind_text(getLastModifiedStmt, 1, path, -1, SQLITE_TRANSIENT);
    
    int ret = sqlite3_step(getLastModifiedStmt);
    if (ret != SQLITE_ROW) {
        // not found
        return -1;
    }
    sqlite_int64 value = sqlite3_column_int64(getLastModifiedStmt, 1);
    
    // reset statement for future use
    sqlite3_clear_bindings(getLastModifiedStmt);
    sqlite3_reset(getLastModifiedStmt);
    
    return value;
}

/*
 * Check return values for erros
 */
bool Database::checkReturn(int ret)
{
    if (!(ret == SQLITE_OK || ret == SQLITE_DONE)) {
        std::cerr << "sqlite error ("<< ret << "): " << sqlite3_errmsg(db) << std::endl;
        return false;
    }
    return true;
}

