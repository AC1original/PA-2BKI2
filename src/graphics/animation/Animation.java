package graphics.animation;
import main.GamePanel;
import main.Main;
import utils.Logger;
import utils.Timed;

import java.awt.*;
import java.util.Map;

public abstract class Animation {
    public abstract AnimationFrame[] getFrames();
    public abstract int getDelay();
    public abstract boolean drawAnimation();
    public abstract void onFrameUpdate(AnimationFrame frame);
    private GamePanel gp;
    private Graphics g;
    private int counter = 1;
    private int index = 0;

    public boolean isValid() {
        if (getFrames().length == 0) {
            Logger.log("AnimationManager: Failed to start animation. No animation frames.", true);
            return false;
        } else if (getDelay() <= 0) {
            Logger.log("AnimationManager: Failed to start animation. Delay is shorter than 1.", true);
            return false;
        }
        return true;
    }

    public void drawAnimation(Graphics g) {
        this.g = g;
        if (!drawAnimation() || !isAllowedToUpdate()) {
            return;
        }
        g.drawImage(getFrames()[index].getImage(), getLocation().x, getLocation().y, getFrames()[index].getImgWidth(), getFrames()[index].getImgHeight(), null);
    }

    @Timed(delay = 1)
    public void updateIndex() {
        if (isAllowedToUpdate()) counter++;
        if (counter >= getDelay()) {
            counter = 1;
            if (index < getFrames().length-1) {
                index++;
                onFrameUpdate(getFrames()[index]);
            } else {
                index = 0;
            }
        }
    }

    public Point getLocation() {
        return new Point(0, 0);
    }

    public Graphics getGraphics() {
        return g;
    }

    public void onPlay(GamePanel gp) {
        this.gp = gp;
    }
    public void onStop() {}

    public GamePanel getGamePanel() {
        return gp;
    }

    private boolean isAllowedToUpdate() {
        return gp != null && gp.getGameRenderer().getFrame().isFocused();
    }
}
