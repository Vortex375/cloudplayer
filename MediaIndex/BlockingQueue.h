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


#ifndef PATHQUEUE_H
#define PATHQUEUE_H

#include <QtCore/QObject>
#include <QtCore/QMutex>
#include <QtCore/QQueue>
#include <QtCore/QWaitCondition>

#include <boost/filesystem.hpp>

using namespace boost::filesystem;

class BlockingQueue {
public:
    BlockingQueue();
    virtual ~BlockingQueue();
    
    void enqueue(const path &p);
    path dequeue();
    
private:
    QWaitCondition cond;
    QMutex mutex;
    QQueue<path> queue;
};

#endif // PATHQUEUE_H
