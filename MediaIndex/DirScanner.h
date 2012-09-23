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


#ifndef DIRSCANNER_H
#define DIRSCANNER_H
#include <QtCore/QThread>
#include <boost/filesystem.hpp>

#include "Stats.h"
#include "BlockingQueue.h"

class DirScanner : public QThread{
Q_OBJECT
    public:
    DirScanner(BlockingQueue *q, char *p, Stats *s);
    virtual ~DirScanner();
protected:
    virtual void run();
private:
    BlockingQueue *queue;
    Stats *stats;
    boost::filesystem::path path;
};

#endif // DIRSCANNER_H
