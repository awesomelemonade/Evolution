package lemon.evolution.ui.beta;

import lemon.engine.font.CommonFonts;
import lemon.engine.math.Box2D;
import lemon.engine.math.GridLayout;
import lemon.engine.math.MathUtil;
import lemon.engine.render.CommonRenderables;
import lemon.engine.toolbox.Color;
import lemon.engine.toolbox.Disposable;
import lemon.engine.toolbox.Disposables;
import lemon.futility.FObservable;
import lemon.futility.FSetWithEvents;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.function.Consumer;

public class UIButtonList extends AbstractUIChildComponent {
	private final FSetWithEvents<UIComponent> mutableComponents = new FSetWithEvents<>();
	private final FObservable<Float> scrollPercentage = new FObservable<>(0f);

	public UIButtonList(UIComponent parent, List<ButtonInfo> buttons, Box2D buttonListBox, int visibleButtons, float spacing) {
		super(parent);
		// View
		disposables.add(mutableComponents.onAdd(component -> children().add(component)));
		disposables.add(mutableComponents.onRemove(Disposable::dispose));
		// Scroll
		disposables.add(input().mouseScrollEvent().add(event -> {
			event.glfwWindow().pollMouse((mouseX, mouseY) -> {
				if (buttonListBox.intersect(mouseX, mouseY)) {
					scrollPercentage.setValue(MathUtil.saturate(
							scrollPercentage.getValue() + (float) (-event.yOffset() * 0.1f)));
				}
			});
		}));
		// Buttons & Scroll Bar GUI
		var buttonsBox = Box2D.ofLeftBox(buttonListBox, 0.90f);
		var scrollBox = Box2D.ofRightBox(buttonListBox, 0.05f);
		var scrollBarHeight = 0.05f;
		var disposeOnScroll = new Disposables();
		// Scroll Bar Fixed GUI
		children().add(new UIRenderable(this, () -> CommonRenderables.renderTransparentQuad2D(scrollBox, new Color(0f, 0f, 0f, 0.25f))));
		// Mutable Components
		disposables.add(scrollPercentage.onChangeAndRun(percentage -> {
			disposeOnScroll.dispose();
			// Construct Buttons
			var layout = new GridLayout(buttonsBox, visibleButtons, 1, spacing);
			var offsetButtons = (int) (percentage * (buttons.size() - visibleButtons));
			for (int i = 0; i < visibleButtons; i++) {
				var buttonInfo = buttons.get(offsetButtons + (visibleButtons - i) - 1);
				var buttonBox = layout.getBox(i, 0);
				var buttonTextBox = Box2D.ofInner(buttonBox, 0f, 0f, 10f, 8f);
				mutableComponents.add(new UIButton(this, buttonBox, uiButton -> buttonInfo.runnable().run(), Color.WHITE));
				mutableComponents.add(UIText.ofHeightCenterAligned(this, CommonFonts.freeSansTightened(), buttonInfo.text(), buttonTextBox, Color.BLACK));
			}
			// Construct Scroll Bar
			var scrollMiddlePercentage = ((1.0f - scrollBarHeight) * (1.0f - percentage)) + scrollBarHeight / 2f;
			var scrollBarBox = Box2D.ofVerticalBox(scrollBox, scrollMiddlePercentage, scrollBarHeight);
			mutableComponents.add(new UIRenderable(this, () -> CommonRenderables.renderQuad2D(scrollBarBox, Color.WHITE)));
			// Deconstruct components after update
			disposeOnScroll.add(mutableComponents::clear);
		}));
		// Scroll Bar mouse events
		var disposeOnRelease = disposables.add(new Disposables());
		Consumer<Float> scrollBarUpdater = mouseY -> {
			var scrollRangeHeight = scrollBox.height() - scrollBarHeight;
			var newPercentage = 1.0f - (mouseY - scrollBox.y() - scrollBarHeight / 2.0f) / scrollRangeHeight;
			scrollPercentage.setValue(MathUtil.saturate(newPercentage));
		};
		// On Press
		disposables.add(input().mouseButtonEvent().add(mouseButtonEvent -> {
			if (mouseButtonEvent.button() == GLFW.GLFW_MOUSE_BUTTON_1 && mouseButtonEvent.action() == GLFW.GLFW_PRESS) {
				mouseButtonEvent.glfwWindow().pollMouse((pressX, pressY) -> {
					if (scrollBox.intersect(pressX, pressY)) {
						disposeOnRelease.add(input().cursorPositionEvent().add(cursorPositionEvent -> {
							var mouseY = (float) (cursorPositionEvent.glfwWindow().getHeight() - cursorPositionEvent.y());
							scrollBarUpdater.accept(mouseY);
						}));
						scrollBarUpdater.accept(pressY);
					}
				});
			}
		}));
		// On Release
		disposables.add(input().mouseButtonEvent().add(releaseButtonEvent -> {
			if (releaseButtonEvent.button() == GLFW.GLFW_MOUSE_BUTTON_1 && releaseButtonEvent.action() == GLFW.GLFW_RELEASE) {
				disposeOnRelease.dispose();
			}
		}));
	}

	public static record ButtonInfo(String text, Runnable runnable) {}
}
