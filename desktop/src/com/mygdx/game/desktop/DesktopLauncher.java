package com.mygdx.game.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.mygdx.game.BludBourne;


public class DesktopLauncher {
    public static void main (String[] arg) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
	/*config.title = "BludBourne";
		config.useOpenGL3(false,3,2);
		config.width = 800;
		config.height = 600;*/
        new Lwjgl3Application(new BludBourne(), config);

    }
}
