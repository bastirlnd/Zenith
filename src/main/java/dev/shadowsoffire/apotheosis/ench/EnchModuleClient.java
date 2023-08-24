package dev.shadowsoffire.apotheosis.ench;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.library.EnchLibraryScreen;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchScreen;
import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings("deprecation")
public class EnchModuleClient {

    static BlockHitResult res = BlockHitResult.miss(Vec3.ZERO, Direction.NORTH, BlockPos.ZERO);

    public static void tooltips() {
        ItemTooltipCallback.EVENT.register((stack, context, tooltip) -> {
            Item i = stack.getItem();
            if (i == Items.COBWEB) tooltip.add(Component.translatable("info.apotheosis.cobweb").withStyle(ChatFormatting.GRAY));
            else if (i == Ench.Items.PRISMATIC_WEB) tooltip.add(Component.translatable("info.apotheosis.prismatic_cobweb").withStyle(ChatFormatting.GRAY));
            else if (i instanceof BlockItem) {
                Block block = ((BlockItem) i).getBlock();
                Level world = Minecraft.getInstance().level;
                if (world == null || Minecraft.getInstance().player == null) return;
                BlockPlaceContext ctx = new BlockPlaceContext(world, Minecraft.getInstance().player, InteractionHand.MAIN_HAND, stack, res){};
                BlockState state = null;
                try {
                    state = block.getStateForPlacement(ctx);
                }
                catch (Exception ex) {
                    EnchModule.LOGGER.debug(ex.getMessage());
                    StackTraceElement[] trace = ex.getStackTrace();
                    for (StackTraceElement traceElement : trace)
                        EnchModule.LOGGER.debug("\tat " + traceElement);
                }

                if (state == null) state = block.defaultBlockState();
                float maxEterna = EnchantingStatRegistry.getMaxEterna(state, world, BlockPos.ZERO);
                float eterna = EnchantingStatRegistry.getEterna(state, world, BlockPos.ZERO);
                float quanta = EnchantingStatRegistry.getQuanta(state, world, BlockPos.ZERO);
                float arcana = EnchantingStatRegistry.getArcana(state, world, BlockPos.ZERO);
                float rectification = EnchantingStatRegistry.getQuantaRectification(state, world, BlockPos.ZERO);
                int clues = EnchantingStatRegistry.getBonusClues(state, world, BlockPos.ZERO);
                if (eterna != 0 || quanta != 0 || arcana != 0 || rectification != 0 || clues != 0) {
                    tooltip.add(Component.translatable("info.apotheosis.ench_stats").withStyle(ChatFormatting.GOLD));
                }
                if (eterna != 0) {
                    if (eterna > 0) {
                        tooltip.add(Component.translatable("info.apotheosis.eterna.p", String.format("%.2f", eterna), String.format("%.2f", maxEterna)).withStyle(ChatFormatting.GREEN));
                    }
                    else tooltip.add(Component.translatable("info.apotheosis.eterna", String.format("%.2f", eterna)).withStyle(ChatFormatting.GREEN));
                }
                if (quanta != 0) {
                    tooltip.add(Component.translatable("info.apotheosis.quanta" + (quanta > 0 ? ".p" : ""), String.format("%.2f", quanta)).withStyle(ChatFormatting.RED));
                }
                if (arcana != 0) {
                    tooltip.add(Component.translatable("info.apotheosis.arcana" + (arcana > 0 ? ".p" : ""), String.format("%.2f", arcana)).withStyle(ChatFormatting.DARK_PURPLE));
                }
                if (rectification != 0) {
                    tooltip.add(Component.translatable("info.apotheosis.rectification" + (rectification > 0 ? ".p" : ""), String.format("%.2f", rectification)).withStyle(ChatFormatting.YELLOW));
                }
                if (clues != 0) {
                    tooltip.add(Component.translatable("info.apotheosis.clues" + (clues > 0 ? ".p" : ""), String.format("%d", clues)).withStyle(ChatFormatting.DARK_AQUA));
                }
            }
            else if (i == Items.ENCHANTED_BOOK) {
                var enchMap = EnchantmentHelper.getEnchantments(stack);
                if (enchMap.size() == 1) {
                    var ench = enchMap.keySet().iterator().next();
                    int lvl = enchMap.values().iterator().next();
                    if (!FabricLoader.getInstance().isModLoaded("enchdesc")) {
                        if (Apotheosis.MODID.equals(BuiltInRegistries.ENCHANTMENT.getKey(ench).getNamespace())) {
                            tooltip.add(Component.translatable(ench.getDescriptionId() + ".desc").withStyle(ChatFormatting.DARK_GRAY));
                        }
                    }
                    var info = EnchModule.getEnchInfo(ench);
                    Object[] args = new Object[4];
                    args[0] = boolComp("info.apotheosis.discoverable", info.isDiscoverable());
                    args[1] = boolComp("info.apotheosis.lootable", info.isLootable());
                    args[2] = boolComp("info.apotheosis.tradeable", info.isTradeable());
                    args[3] = boolComp("info.apotheosis.treasure", info.isTreasure());
                    if (context.isAdvanced()) {
                        tooltip.add(Component.translatable("%s \u2507 %s \u2507 %s \u2507 %s", args[0], args[1], args[2], args[3]).withStyle(ChatFormatting.DARK_GRAY));
                        tooltip.add(Component.translatable("info.apotheosis.book_range", info.getMinPower(lvl), info.getMaxPower(lvl)).withStyle(ChatFormatting.GREEN));
                    }
                    else {
                        tooltip.add(Component.translatable("%s \u2507 %s", args[2], args[3]).withStyle(ChatFormatting.DARK_GRAY));
                    }
                }
            }
        });

    }

    private static Component boolComp(String key, boolean flag) {
        return Component.translatable(key + (flag ? "" : ".not")).withStyle(Style.EMPTY.withColor(flag ? 0x108810 : 0xAA1616));
    }

    public static void init() {
        tooltips();
        //BlockEntityRenderers.register(BlockEntityType.ENCHANTING_TABLE, EnchantTableRenderer::new);
        MenuScreens.register(Apoth.Menus.ENCHANTING_TABLE, ApothEnchScreen::new);
        MenuScreens.register(Apoth.Menus.LIBRARY, EnchLibraryScreen::new);
    }
/* //TODO particles
    public static void particleFactories(RegisterParticleProvidersEvent e) {
        e.registerSpriteSet(Particles.ENCHANT_FIRE.get(), EnchantmentTableParticle.Provider::new);
        e.registerSpriteSet(Particles.ENCHANT_WATER.get(), EnchantmentTableParticle.Provider::new);
        e.registerSpriteSet(Particles.ENCHANT_SCULK.get(), EnchantmentTableParticle.Provider::new);
        e.registerSpriteSet(Particles.ENCHANT_END.get(), EnchantmentTableParticle.Provider::new);
    }*/
}
