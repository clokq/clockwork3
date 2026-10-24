# Clockwork (Fabric, Minecraft 1.21.11, Java 21)

Right Shift opens the GUI (in a world, no other screen open). Left-click a module = toggle, right-click = settings,
scroll = pan, right-click a hue bar = rainbow. Config: `config/clockwork.json`.

Build: push to GitHub and run Actions -> build, or locally `gradle build` with JDK 21 -> `build/libs/clockwork-0.1.0.jar`.
Needs Fabric API for 1.21.11. ESP boxes render through walls by default (per-module "Through walls" toggle).
Modpack: see `modpack/`.
