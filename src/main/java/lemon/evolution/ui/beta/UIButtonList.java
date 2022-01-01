package lemon.evolution.ui.beta;

import lemon.engine.draw.CommonDrawables;
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

import java.util.List;

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
							scrollPercentage.getValue() + (float) (event.yOffset() * 0.1f)));
				}
			});
		}));
		// Buttons & Scroll Bar GUI
		var buttonsBox = Box2D.ofLeftBox(buttonListBox, 0.90f);
		var scrollBox = Box2D.ofRightBox(buttonListBox, 0.05f);
		children().add(new UIRenderable(this, () -> CommonRenderables.renderQuad2D(scrollBox, new Color(0.3f, 0.3f, 0.3f))));
		var disposeOnScroll = new Disposables();
		disposeOnScroll.add(scrollPercentage.onChangeAndRun(percentage -> {
			disposeOnScroll.dispose();
			// Construct Buttons
			var layout = new GridLayout(buttonsBox, visibleButtons, 1, spacing);
			var offsetButtons = (int) (percentage * buttons.size());
			for (int i = 0; i < visibleButtons; i++) {
				var buttonInfo = buttons.get(offsetButtons + i);
				var buttonBox = layout.getBox(i, 0);
				var buttonTextBox = Box2D.ofInner(buttonBox, 0f, 0f, 8f, 5f);
				mutableComponents.add(new UIButton(this, buttonBox, uiButton -> buttonInfo.runnable().run(), Color.WHITE));
				mutableComponents.add(UIText.ofHeightCenterAligned(this, CommonFonts.freeSansTightened(), buttonInfo.text(), buttonTextBox, Color.BLACK));
			}
			// Construct Scroll Bar
			var scrollBarBox = Box2D.ofInner(scrollBox, 0f, 0f, 50f, 50f);
			mutableComponents.add(new UIRenderable(this, () -> CommonRenderables.renderQuad2D(scrollBarBox, Color.WHITE)));
			// Deconstruct components after update
			disposeOnScroll.add(mutableComponents::clear);
		}));
		// Scroll Bar Fixed GUI
		final var scrollRate = 0.01f;
		var upArrowBox = Box2D.ofInner(scrollBox, 0f, 0f, 0f, scrollBox.height() - 50f);
		children().add(new UIButton(this, upArrowBox, button -> {
			scrollPercentage.setValue(MathUtil.saturate(scrollPercentage.getValue() - scrollRate));
		}, () -> CommonRenderables.renderDrawable2D(upArrowBox, Color.WHITE, CommonDrawables.COLORED_TRIANGLE)));
		var downArrowBox = Box2D.ofInner(scrollBox, 0f, 0f, scrollBox.height() - 50f, 0f);
		children().add(new UIButton(this, downArrowBox, button -> {
			scrollPercentage.setValue(MathUtil.saturate(scrollPercentage.getValue() + scrollRate));
		}, () -> CommonRenderables.renderDrawable2D(downArrowBox, Color.WHITE, CommonDrawables.COLORED_TRIANGLE)));
	}

	public static record ButtonInfo(String text, Runnable runnable) {}
}
