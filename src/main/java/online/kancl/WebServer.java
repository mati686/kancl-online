package online.kancl;

import online.kancl.controller.MainPageController;
import online.kancl.server.ExceptionHandler;
import spark.Request;
import spark.Response;
import spark.Spark;

public class WebServer
{
	private final int port;
	private final ZoomHook zoomHook;
	private final MainPageController mainPageController;
	private final ExceptionHandler exceptionHandler;

	public WebServer(int port, ZoomHook zoomHook, MainPageController mainPageController, ExceptionHandler exceptionHandler)
	{
		this.port = port;
		this.zoomHook = zoomHook;
		this.mainPageController = mainPageController;
		this.exceptionHandler = exceptionHandler;
	}

	public void start() {
		Spark.port(port);

		Spark.staticFiles.externalLocation("web");
		Spark.get("/", mainPageController::get);
		Spark.get("/zoomhook", this::callZoomHook);
		Spark.post("/zoomhook", this::callZoomHook);

		Spark.exception(Exception.class, exceptionHandler::handleException);

		Spark.init();
	}

	private String callZoomHook(Request request, Response response) {
		zoomHook.handleZoomMessage(request);
		return "OK";
	}
}
