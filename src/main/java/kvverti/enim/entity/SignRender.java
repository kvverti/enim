package kvverti.enim.entity;

import java.util.Set;
import java.util.HashSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiUtilRenderComponents;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.IChatComponent;

import kvverti.enim.modelsystem.EntityState;

public class SignRender extends SignLikeRender<TileEntitySign> {

	public SignRender(String modDomain, String stateFile) {

		super(modDomain, stateFile, Blocks.standing_sign);
	}

	@Override
	public void postRender(TileEntitySign tile, EntityInfo info) {

		//BEGIN MAGIC
		FontRenderer render = this.getFontRenderer();
		float f = 0.667f * 0.015625f;
		GlStateManager.scale(f, f, f);
		if(tile.getBlockType() == Blocks.standing_sign) GlStateManager.translate(0.0f, 0.0f, -4.01f);
		else GlStateManager.translate(0.0f, 29.0f, 37.99f);
		GlStateManager.depthMask(false);
		for(int line = 0; line < tile.signText.length; line++) {

			if(tile.signText[line] != null) {

				IChatComponent raw = tile.signText[line];
				List<IChatComponent> frmtd =
					GuiUtilRenderComponents.func_178908_a(raw, 90, render, false, true);
				String text = frmtd != null && !frmtd.isEmpty() ? frmtd.get(0).getFormattedText() : "";
				if(tile.lineBeingEdited == line) text = "> " + text + " <";
				render.drawString(text, -render.getStringWidth(text) / 2,
					(line * 10) - (tile.signText.length * 25), 0);
			}
		}
		GlStateManager.depthMask(true);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		//END MAGIC
	}
}