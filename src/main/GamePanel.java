package main;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import entity.EntityHelper;
import entity.player.Player;
import graphics.GameRenderer;
import graphics.animation.AnimationManager;
import level.LevelManager;
import user.UserKeyboardInput;
import utils.Logger;
import utils.Timed;

public class GamePanel {
	private static final List<Object> registered = new ArrayList<>();
	private static final Map<Method, Long> timed = new HashMap<>();
	private GameStates gameState = GameStates.MENU;
	private static boolean running = false;
	private final AnimationManager animationManager = register(new AnimationManager(this));
	private final Player player = register(new Player());
	private final EntityHelper entityHelper = register(new EntityHelper());
	private final UserKeyboardInput uKey = new UserKeyboardInput(this);
	private final LevelManager levelManager = register(new LevelManager(this));
	private final GameRenderer gr = register(new GameRenderer(this));

	
	public void run() throws InterruptedException {
		entityHelper.spawn(player);
		register(this);
		running = true;

		Thread rendererThread = new Thread(gr);
		rendererThread.start();

        while (running) {
			Thread.sleep(8);
        	tick();
        }
	}

	public void tick() {
		entityHelper.tick();
			timed.forEach((method, timeStamp) -> {
				if (method == null) return;
				if (System.currentTimeMillis() - timeStamp >= method.getAnnotation(Timed.class).delay()) {
                    try {
						Object instance = registered.stream()
								.filter(obj -> method.getDeclaringClass().isInstance(obj))
								.findFirst()
								.orElse(null);
						if (instance == null) return;
                        method.invoke(instance);
						timed.replace(method, System.currentTimeMillis());
                    } catch (ReflectiveOperationException e) {
                        Logger.log("GamePanel: Failed to tick \"Timed\" annotation", true);
                    }
                }
			});
	}
	
	public<T> T register(T clazz) {
		registered.add(clazz);
		Logger.log("GamePanel: Registered class " + clazz.getClass().getSimpleName());

		for (Method methods : clazz.getClass().getMethods()) {
			if (methods.isAnnotationPresent(Timed.class)) {
				timed.put(methods, System.currentTimeMillis());
			}
		}
		return clazz;
	}

	public<T> void unregister(T clazz) {
		timed.forEach((K, V) -> {
			if (K.getDeclaringClass().equals(clazz.getClass())) {
				timed.remove(K);
			}
		});
		registered.remove(clazz);
		Logger.log("GamePanel: Unregistered class " + clazz.getClass().getSimpleName());
	}
	
	public void setGameState(GameStates gameState) {
		this.gameState = gameState;
	}
	public GameStates getGameState() {
		return gameState;
	}
	
	public void forceStop() {
		//save logic
		running = false;
	}

	public Player getPlayer() {
		return player;
	}

	public EntityHelper getEntityHelper() {
		return entityHelper;
	}

	public static boolean isRunning() {
		return running;
	}

	public AnimationManager getAnimationManager() {
		return animationManager;
	}

	public UserKeyboardInput getUserKeyboardInput() {
		return uKey;
	}

	public GameRenderer getGameRenderer() {
		return gr;
	}

	public LevelManager getLevelManager() {
		return levelManager;
	}
}
