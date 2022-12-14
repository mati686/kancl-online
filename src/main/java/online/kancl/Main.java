package online.kancl;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.FileLoader;
import online.kancl.db.ConnectionProvider;
import online.kancl.db.SchemaCreator;
import online.kancl.db.TransactionJobRunner;
import online.kancl.db.UserStorage;
import online.kancl.db.*;
import online.kancl.objects.GridData;
import online.kancl.objects.Wall;
import online.kancl.objects.ZoomObject;
import online.kancl.page.api.OfficeController;
import online.kancl.page.login.LoginController;
import online.kancl.page.login.LoginInfo;
import online.kancl.page.logout.LogoutController;
import online.kancl.page.main.MainPageController;
import online.kancl.page.main.Meetings;
import online.kancl.page.recreatedb.RecreateDbController;
import online.kancl.page.registration.RegistrationController;
import online.kancl.page.registration.RegistrationInfo;
import online.kancl.page.userpage.UserPageController;
import online.kancl.page.zoomhook.ZoomHookController;
import online.kancl.server.Controller;
import online.kancl.server.ExceptionHandler;
import online.kancl.server.WebServer;
import online.kancl.server.template.PebbleExtension;
import online.kancl.server.template.PebbleTemplateRenderer;
import online.kancl.util.DirectoryHashCalculator;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

public class Main {

    public static final Path TEMPLATE_DIRECTORY = Paths.get("src", "main", "resources");
    public static final Path SQL_SCRATCH_DIRECTORY = Paths.get("src", "main", "resources", "sql");
    private static final Path DB_DIRECTORY = Paths.get("db");
    private static final String DB_NAME = "data";

    public static void main(String[] args) {
        PebbleTemplateRenderer pebbleTemplateRenderer = createPebbleTemplateRenderer(TEMPLATE_DIRECTORY);

        ConnectionProvider connectionProvider = ConnectionProvider.forDatabaseInFile(DB_DIRECTORY, DB_NAME);
        var directoryHashCalculator = new DirectoryHashCalculator();
        var schemaCreator = new SchemaCreator(directoryHashCalculator, connectionProvider, SQL_SCRATCH_DIRECTORY);
        schemaCreator.recreateSchemaIfNeeded();

        var transactionJobRunner = new TransactionJobRunner(connectionProvider);

        var meetings = new Meetings();
        var gridData = new GridData();
        addStartingWalls(gridData);

        var webServer = new WebServer(8081, new ExceptionHandler(), transactionJobRunner, "/login");
        webServer.addRoute("/", () -> new MainPageController(pebbleTemplateRenderer));
        webServer.addRoute("/zoomhook", () -> new ZoomHookController(meetings));
        webServer.addRoute("/recreateDb", () -> new RecreateDbController(schemaCreator));
        webServer.addRoute("/user", (dbRunner) -> new UserPageController(pebbleTemplateRenderer, new UserStorage(dbRunner)));
        webServer.addRoute("/register", (dbRunner) -> new RegistrationController(pebbleTemplateRenderer, transactionJobRunner, new RegistrationInfo(), new UserStorage(dbRunner)));
        webServer.addRoute("/login", createLoginController(pebbleTemplateRenderer, transactionJobRunner, gridData));
        webServer.addRoute("/logout", () -> new LogoutController(gridData));
        webServer.addRoute("/api/office", () -> new OfficeController(gridData));

        webServer.addPublicPaths("/login", "/register", "/zoomhook", "/recreateDb");

        webServer.start();

        System.out.println("Server running");
    }

    private static Function<DatabaseRunner, Controller> createLoginController(PebbleTemplateRenderer pebbleTemplateRenderer, TransactionJobRunner transactionJobRunner, GridData gridData) {
        return (dbRunner) -> {
            var userStorage = new UserStorage(dbRunner);
            return new LoginController(pebbleTemplateRenderer,
                    transactionJobRunner, new LoginInfo(), gridData, userStorage);
        };
    }

    public static PebbleTemplateRenderer createPebbleTemplateRenderer(Path templateDirectory) {
        var pebbleTemplateLoader = new FileLoader();
        pebbleTemplateLoader.setPrefix(templateDirectory.toAbsolutePath().toString());
        var pebbleEngine = new PebbleEngine.Builder()
                .loader(pebbleTemplateLoader)
                .extension(new PebbleExtension())
                .cacheActive(false)
                .build();

        return new PebbleTemplateRenderer(pebbleEngine);
    }

    private static void addStartingWalls(GridData gridData){
        List<String> zoomLinks = extracted();

        ZoomObject zoomObject = new ZoomObject(25, 0, zoomLinks.get(0));
        ZoomObject zoomObject2 = new ZoomObject(0, 0, zoomLinks.get(1));

        List<Wall> walls = Arrays.asList(
                new Wall(0, 4),
                new Wall(1, 4),
                new Wall(2, 4),
                new Wall(3, 4),
                new Wall(4, 4),
                new Wall(5, 4),
                new Wall(6, 4),
                new Wall(6, 0),
                new Wall(6, 1),
                new Wall(6, 2),
                new Wall(19, 0),
                new Wall(19, 1),
                new Wall(19, 2),
                new Wall(19, 4),
                new Wall(20, 4),
                new Wall(21, 4),
                new Wall(22, 4),
                new Wall(23, 4),
                new Wall(24, 4),
                new Wall(25, 4),
                new Wall(0, 13),
                new Wall(1, 13),
                new Wall(2, 13),
                new Wall(3, 13),
                new Wall(4, 13),
                new Wall(5, 13),
                new Wall(6, 13),
                new Wall(6, 15),
                new Wall(6, 16),
                new Wall(6, 17)
        );

        System.out.println(zoomLinks);
        gridData.addWallsList(walls);
        gridData.addZoom(zoomObject);
        gridData.addZoom(zoomObject2);
    }

    private static List<String> extracted() {
        List<String> zoomLinks = new ArrayList<>();
        try {
            File myObj = new File("zoomlinks.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                zoomLinks.add(data);
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            zoomLinks.add("https://www.google.com/");
            zoomLinks.add("https://www.google.com/");
        }

        return zoomLinks;
    }
}
