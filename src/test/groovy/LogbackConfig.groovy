/*
appender("mongoAppender", LogbackMongoAppender) {
    hosts = [new ServerAddress("127.0.0.1")]
    databaseName = "testsLogs"
    collectionName = "log_from__${timestamp("dd_MM_yyyy__HH_mm_ss")}"
}
*/

root(OFF, [])
