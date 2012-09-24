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


#include "DirScanner.h"

DirScanner::DirScanner (BlockingQueue *q, Stats *s, char *p) : QThread() {
    queue = q;
    path = absolute(p); // always scan absolute paths
    stats = s;
}


DirScanner::~DirScanner() {

}

void DirScanner::run() {
    for (boost::filesystem::recursive_directory_iterator iter(path), end; iter != end; iter++) {
        if (is_regular_file(iter->path())) {
            queue->enqueue(iter->path());
            stats->incrementFound();
        }
    }
    queue->enqueue(boost::filesystem::path());
}

#include "DirScanner.moc"