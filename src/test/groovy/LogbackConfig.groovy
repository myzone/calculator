import com.mongodb.ServerAddress
import com.myzone.utils.logging.LogbackMongoAppender

class GroovyAppender extends LogbackMongoAppender {

    @Override
    public List<ServerAddress> getHosts() {
        return [new ServerAddress("127.0.0.1")]
    }

    @Override
    public String getDatabaseName() {
        return "testsLogs";
    }

    @Override
    public String getCollectionName() {
        return "log_from__${new Date().format("dd_MM_yyyy__HH_mm_ss")}"
    }
}

appender("mongoAppender", GroovyAppender)
root(ALL, ["mongoAppender"])
