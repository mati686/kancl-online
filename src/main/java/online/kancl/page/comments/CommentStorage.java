package online.kancl.page.comments;

import online.kancl.db.DatabaseRunner;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class CommentStorage {

    public static List<Comment> loadAllComments(DatabaseRunner dbRunner) {
        return dbRunner.selectAll(
                "SELECT * FROM Comment ORDER BY id",
                CommentStorage::handle);
    }

    public static void add(DatabaseRunner dbRunner, Comment comment) {
        dbRunner.update(
                "INSERT INTO Comment(author, message) VALUES (?, ?)",
                comment.author(), comment.message());
    }

    private static Comment handle(ResultSet rs) throws SQLException {
        return new Comment(
                Optional.of(rs.getLong("id")),
                rs.getString("author"),
                rs.getString("message")
        );
    }
}