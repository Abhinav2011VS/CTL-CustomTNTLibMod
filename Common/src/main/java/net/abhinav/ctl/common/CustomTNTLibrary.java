package net.abhinav.ctl.common;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = CustomTNTLibrary.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CustomTNTLibrary {
    public static final String MOD_ID = "ctl";

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);

    @SubscribeEvent
    public static void registerBlocks(final RegistryEvent.Register<Block> event) {
        event.getRegistry().registerAll(
                new CustomTNTBlock(2.0f, 2.0f, Block.Settings.copy(net.minecraft.block.Blocks.TNT)).setRegistryName(new Identifier(MOD_ID, "custom_tnt_block"))
        );
    }

    @SubscribeEvent
    public static void registerItems(final RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(
                new CustomTNTItem(new Item.Settings().group(net.minecraft.item.ItemGroup.MISC)).setRegistryName(new Identifier(MOD_ID, "custom_tnt_item"))
        );
    }

    public static class CustomTNTBlock extends Block {
        private final float tntPower;
        private final float blockStrength;

        public CustomTNTBlock(float tntPower, float blockStrength, Settings settings) {
            super(settings);
            this.tntPower = tntPower;
            this.blockStrength = blockStrength;
        }

        @Override
        public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, Direction hitDirection) {
            if (!world.isClient) {
                explode(world, pos, player);
                world.removeBlock(pos, false);
            }
            return ActionResult.SUCCESS;
        }

        private void explode(World world, BlockPos pos, PlayerEntity player) {
            world.playSound(null, pos, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);

            if (!world.isClient) {
                TntEntity tntEntity = new TntEntity(world, pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, player);
                tntEntity.setFuse(80); // Set fuse time (in ticks)

                // Multiply the explosion power by the custom TNT power
                float explosionPower = tntEntity.getFuse() * tntPower;

                world.spawnEntity(tntEntity);
                world.createExplosion(tntEntity, pos.getX(), pos.getY(), pos.getZ(), explosionPower, Explosion.DestructionType.DESTROY);
            }
        }

        @Override
        public void onBlockDestroyedByPlayer(World worldIn, BlockPos pos, BlockState state) {
            explode(worldIn, pos, null);
            super.onBlockDestroyedByPlayer(worldIn, pos, state);
        }

        @Override
        public BlockState getStateForPlacement(BlockItemUseContext context) {
            return super.getStateForPlacement(context);
        }
    }

    public static class CustomTNTItem extends Item {
        public CustomTNTItem(Settings settings) {
            super(settings);
        }
    }
}
