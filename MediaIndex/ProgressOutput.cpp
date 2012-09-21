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


#include "ProgressOutput.h"
#include <QTimer>
#include <QDebug>

ProgressOutput::ProgressOutput(Stats *s) : out(stdout) {
    stats = s;
    QTimer *timer = new QTimer(this);
    timer->setInterval(33);
    connect(timer, SIGNAL(timeout()), this, SLOT(output()));
    timer->start();
}

ProgressOutput::~ProgressOutput() {

}

void ProgressOutput::output() {
    out << "\33[2K\r";
    out << "Indexing: " << (int) ((stats->getProcessed() / (double) stats->getFound()) * 100) << "% (" << stats->getProcessed() << "/" << stats->getFound() << ")";
    out.flush();
}


#include "ProgressOutput.moc"