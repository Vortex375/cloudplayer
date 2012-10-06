/*
    <one line to give the program's name and a brief idea of what it does.>
    Copyright (C) 2012  Benjamin Schmitz <benni@wolpzone.de>

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


#include "DbPathIterator.h"

DbPathIterator::DbPathIterator(sqlite3_stmt *stmt)
{
    this->stmt = stmt;
}

DbPathIterator::~DbPathIterator()
{
    sqlite3_finalize(stmt);
}

bool DbPathIterator::next()
{
    return (sqlite3_step(stmt) == SQLITE_ROW);
}

const char* DbPathIterator::getPath()
{
    return (char*) sqlite3_column_text(stmt, 0);
}
