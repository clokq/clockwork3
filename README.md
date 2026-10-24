# Clockwork (Fabric, Minecraft 26.2)

Right Shift opens the GUI. Left-click a module to toggle it, right-click to open its settings, scroll to pan.
Right-click a colour's hue bar to toggle rainbow. Config is saved to `config/clockwork.json`.

Build (Java 25 + Gradle 9.3+):  `gradle wrapper && ./gradlew build`  -> `build/libs/clockwork-0.1.0.jar`
1.21.11 build: see `variants/1.21.11/`. Modpack: see `modpack/`.
Boxes render through walls by default (toggle "Through walls" per ESP module).

## Version-sensitive spots (written from memory of 26.x names, NOT compiled)
- `util/RenderUtil.java`   RenderType.lines()/debugFilledBox(), ShapeRenderer.renderLineBox. Through-wall ESP needs a no-depth RenderPipeline (see TODO).
- `ClockworkClient.java`   WorldRenderEvents.AFTER_ENTITIES, ResourceLoader.registerBuiltinPack, InputConstants.isKeyDown(Window, key)
- `gui/ClickGui.java`      GuiGraphics / render(), blit(RenderPipelines.GUI_TEXTURED, ...), MouseButtonEvent/KeyEvent
- `mixin/*`                targets use require=0, so a wrong target silently disables that feature instead of crashing.
  - BlockMixin  -> Block.shouldRenderFace (xray)
  - ClientLevelMixin -> getSkyColor (sky tint; 26.x may resolve this via environment attributes)
  - KeyboardInputMixin -> KeyboardInput.tick (freezes your body in freecam)
- Sun/moon clock textures are in `resourcepacks/clockwork_sky/` at both `environment/` and `environment/celestial/`; keep whichever path your 26.2 jar uses.

## Build without installing anything (GitHub Actions)
Create a GitHub repo, upload this folder's contents, open the Actions tab -> "build" -> Run workflow.
When it finishes, download the `clockwork-jar` artifact: that is the mod. (Build errors show in the run log; paste them to Claude.)
