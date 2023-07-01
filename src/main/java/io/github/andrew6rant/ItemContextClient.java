package io.github.andrew6rant;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.andrew6rant.ItemContext.elements;
import static io.github.andrew6rant.ItemContext.invert;

public class ItemContextClient implements ClientModInitializer {

	private static final MinecraftClient client = MinecraftClient.getInstance();

    public static final Logger LOGGER = LoggerFactory.getLogger("itemcontext");

	static {
		ItemContext.configChanged();
	}
	@Override
	public void onInitializeClient() {
		/*HudRenderCallback.EVENT.register(new Identifier("itemcontext:hud_render"), (context, tickDelta) -> {
			assert client.player != null;
			int offsetX = 110; // config 110
			int offsetY = 19; // config 19
			ItemStack activeItemStack = client.player.getMainHandStack();
			ItemStack offhandItemStack = client.player.getOffHandStack();
			PlayerInventory playerInventory = client.player.getInventory();
			int scaledWidth = client.getWindow().getScaledWidth();
			int scaledHeight = client.getWindow().getScaledHeight();

			//System.out.println(activeItem == ItemStack.EMPTY);
			if (!activeItemStack.isEmpty()) {
				int totalCount = playerInventory.count(activeItemStack.getItem());
				if (totalCount > activeItemStack.getMaxCount()) {
					int x = scaledWidth/2 - offsetX;
					int y = scaledHeight - offsetY;
					if (!offhandItemStack.isEmpty()) {
						x = x - 29;
					}
					context.drawItem(activeItemStack, x, y);
					context.drawItemInSlot(client.textRenderer, activeItemStack, x, y, String.valueOf(totalCount));
				}
			}
		});*/

		HudRenderCallback.EVENT.register(new Identifier("itemcontext:hud_render"), (context, tickDelta) -> {
			assert client.player != null;
			for(ItemContext.TickerElement ticker : elements) {
				if(ticker != null) {
					ticker.tick(client.player);
					ticker.render(client, context, client.getWindow(), client.player, invert, client.getTickDelta());
				}
			}
		});
	}
}