#include <QtCore/QCoreApplication>
#include <QtCore/QStringList>
#include <QtCore/QVector>
#include <QtCore/QTextStream>
#include <QtCore/QThread>

#include <iostream>

#include<taglib/fileref.h>
#include<taglib/tag.h>

#include<boost/filesystem.hpp>

#include "Stats.h"
#include "BlockingQueue.h"
#include "DirScanner.h"
#include "Indexer.h"
#include "ProgressOutput.h"

using namespace boost::filesystem;

int main(int argc, char** argv)
{
    QCoreApplication app(argc, argv);
    QTextStream out(stdout);
    out << "Welcome to MediaIndex version 0.1" << endl;
    
    /*QStringList args = app.arguments();
    if (args.size() < 4) {
        std::cout << "Usage: mediaindex <create|update> <database file> <media dirs ...>";
        return 0;
    }*/
    
    //MediaIndex foo;
    
    if (argc < 2) {
        return 0;
    }
    
    Stats stats;
    BlockingQueue pathQueue;
    
    DirScanner scanner(&pathQueue, argv[1], &stats);
    Indexer indexer(&pathQueue, &stats);
    
    QThread *outputThread = new QThread();
    ProgressOutput *output = new ProgressOutput(&stats);
    output->moveToThread(outputThread);
    
    scanner.start();
    out << "Filesystem scan started." << endl;
    indexer.start();
    out << "Indexing started." << endl << endl;
    outputThread->start();
    
    scanner.wait();
    out << endl << "Filesystem scan complete." << endl << endl;
    indexer.wait();
    outputThread->quit();
    out << endl << endl << "Indexing complete." << endl;
    
    return 0;
}
