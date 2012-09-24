#include <QtCore/QCoreApplication>
#include <QtCore/QStringList>
#include <QtCore/QVector>
#include <QtCore/QTextStream>
#include <QtCore/QThread>

#include <iostream>
#include <stdio.h>

#include<taglib/fileref.h>
#include<taglib/tag.h>

#include<boost/filesystem.hpp>

#include "Stats.h"
#include "BlockingQueue.h"
#include "DirScanner.h"
#include "Indexer.h"
#include "ProgressOutput.h"
#include "Database.h"
#include "Updater.h"

using namespace boost::filesystem;

static QTextStream out(stdout);

void printUsage() {
    out << "Usage: mediaindex <create|update> <database file> <media directories ...>" << endl;
}

void createDatabase(char* databasePath, char* dirPath) {
    out << "Creating new database at " << databasePath << endl;
    // check if database file already exists
    if (exists(databasePath)) {
        out << "Error: database file already exists!" << endl;
        out << "'create' creates a new database file. "
            << "If you wish to update an existing database use 'update'." << endl;
        return;
    }
    
    Database db;
    if (!db.open(databasePath)) {
        out << "Error: failed to open database." << endl;
        exit(1);
    };
    
    // create database tables
    if (!db.create()) {
        out << "Error: failed to initialize database." << endl;
        exit(1);
    }
    
    Stats stats;
    BlockingQueue pathQueue;
    
    DirScanner scanner(&pathQueue, dirPath, &stats);
    Indexer indexer(&pathQueue, &db, &stats);
    
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
}

void updateDatabase(char *databasePath, char *dirPath) {
    out << "Open database at " << databasePath << endl;
    // check if database file exists
    if (!exists(databasePath)) {
        out << "Error: database file does not exist!" << endl;
        out << "'update' updates an existing database file. "
            << "If you wish to create a new database use 'create'." << endl;
        return;
    }
    
    Database db;
    if (!db.open(databasePath)) {
        out << "Error: failed to open database." << endl;
        exit(1);
    };
    
    Stats stats;
    BlockingQueue pathQueue;
    
    DirScanner scanner(&pathQueue, dirPath, &stats);
    Updater updater(&pathQueue, &db, &stats);
    
    QThread *outputThread = new QThread();
    ProgressOutput *output = new ProgressOutput(&stats);
    output->moveToThread(outputThread);
    
    scanner.start();
    out << "Filesystem scan started." << endl;
    updater.start();
    out << "Indexing started." << endl << endl;
    outputThread->start();
    
    scanner.wait();
    out << endl << "Filesystem scan complete." << endl << endl;
    updater.wait();
    outputThread->quit();
    out << endl << endl << "Indexing complete." << endl;
}

int main(int argc, char** argv) {
    QCoreApplication app(argc, argv);
    out << "Welcome to MediaIndex v0.1." << endl << endl;
    
    if (argc < 4) {
        printUsage();
        return 0;
    }
    
    if (strcmp(argv[1], "create") == 0) {
        createDatabase(argv[2], argv[3]);
    } else if (strcmp(argv[1], "update") == 0) {
        updateDatabase(argv[2], argv[3]);
    } else {
        printUsage();
    }
    
    return 0;
}