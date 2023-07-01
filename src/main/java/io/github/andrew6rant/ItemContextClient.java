package io.github.andrew6rant;

import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.github.andrew6rant.ItemContext.elements;
import static io.github.andrew6rant.ItemContext.invert;

public class ItemContextClient implements ClientModInitializer {

	private static final MinecraftClient client = MinecraftClient.getInstance();

    public static final Logger LOGGER = LoggerFactory.getLogger("itemcontext");

	static {
		ItemContext.configChanged(); // temporary until I make a config system
	}
	@Override
	public void onInitializeClient() {
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