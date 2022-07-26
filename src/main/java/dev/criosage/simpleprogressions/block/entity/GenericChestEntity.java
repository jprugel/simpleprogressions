package dev.criosage.simpleprogressions.block.entity;

import dev.criosage.simpleprogressions.block.custom.ContainerType;
import dev.criosage.simpleprogressions.block.custom.GenericChestBlock;
import dev.criosage.simpleprogressions.screenhandler.GenericContainerScreenHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.block.entity.ViewerCountManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

public class GenericChestEntity extends LootableContainerBlockEntity implements IAnimatable {
    private DefaultedList<ItemStack> inventory;
    private ViewerCountManager stateManager;
    public ContainerType type;

    public GenericChestEntity(BlockPos pos, BlockState state, ContainerType type) {
        super(ContainerType.getBlockEntityType(type), pos, state);
        this.inventory = DefaultedList.ofSize(type.getSize(), ItemStack.EMPTY);
        this.stateManager = new ViewerCountManager() {
            protected void onContainerOpen(World world, BlockPos pos, BlockState state) {
                dev.criosage.simpleprogressions.block.entity.GenericChestEntity.this.playSound(state, SoundEvents.BLOCK_BARREL_OPEN);
                dev.criosage.simpleprogressions.block.entity.GenericChestEntity.this.setOpen(state, true);
            }

            protected void onContainerClose(World world, BlockPos pos, BlockState state) {
                dev.criosage.simpleprogressions.block.entity.GenericChestEntity.this.playSound(state, SoundEvents.BLOCK_BARREL_CLOSE);
                dev.criosage.simpleprogressions.block.entity.GenericChestEntity.this.setOpen(state, false);
            }

            protected void onViewerCountUpdate(World world, BlockPos pos, BlockState state, int oldViewerCount, int newViewerCount) {
            }

            protected boolean isPlayerViewing(PlayerEntity player) {
                if (player.currentScreenHandler instanceof GenericContainerScreenHandler) {
                    Inventory inventory = ((GenericContainerScreenHandler)player.currentScreenHandler).getInventory();
                    return inventory == dev.criosage.simpleprogressions.block.entity.GenericChestEntity.this;
                } else {
                    return false;
                }
            }
        };
        this.type = type;
    }

    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        if (!this.serializeLootTable(nbt)) {
            Inventories.writeNbt(nbt, this.inventory);
        }
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.deserializeLootTable(nbt)) {
            Inventories.readNbt(nbt, this.inventory);
        }
    }

    public int size() {
        return this.type.getSize();
    }

    public DefaultedList<ItemStack> getInvStackList() {
        return this.inventory;
    }

    public void setInvStackList(DefaultedList<ItemStack> list) {
        this.inventory = list;
    }

    protected Text getContainerName() {
        return Text.translatable(getCachedState().getBlock().getTranslationKey());
    }

    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new GenericContainerScreenHandler(syncId, playerInventory, this, type);
    }

    public void onOpen(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.openContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }

    }

    public void onClose(PlayerEntity player) {
        if (!this.removed && !player.isSpectator()) {
            this.stateManager.closeContainer(player, this.getWorld(), this.getPos(), this.getCachedState());
        }

    }

    public void tick() {
        if (!this.removed) {
            this.stateManager.updateViewerCount(this.getWorld(), this.getPos(), this.getCachedState());
        }
    }

    void setOpen(BlockState state, boolean open) {
        this.world.setBlockState(this.getPos(), (BlockState)state.with(GenericChestBlock.OPEN, open), 3);
    }

    void playSound(BlockState state, SoundEvent soundEvent) {
        Vec3i vec3i = ((Direction)state.get(GenericChestBlock.FACING)).getVector();
        double d = (double)this.pos.getX() + 0.5 + (double)vec3i.getX() / 2.0;
        double e = (double)this.pos.getY() + 0.5 + (double)vec3i.getY() / 2.0;
        double f = (double)this.pos.getZ() + 0.5 + (double)vec3i.getZ() / 2.0;
        this.world.playSound((PlayerEntity)null, d, e, f, soundEvent, SoundCategory.BLOCKS, 0.5F, this.world.random.nextFloat() * 0.1F + 0.9F);
    }
    //region GeckoLib Animation
    private final AnimationFactory factory = new AnimationFactory(this);
    private boolean isAnimOpen = false;
    private <E extends BlockEntity & IAnimatable> PlayState predicate(AnimationEvent<E> event) {
        AnimationController<GenericChestEntity> controller = event.getController();
        controller.transitionLengthTicks = 0;
        if(this.isRemoved()) return PlayState.CONTINUE;
        if(this.getWorld().getBlockState(this.getPos()).get(GenericChestBlock.OPEN)) {
            controller.setAnimation(new AnimationBuilder().addAnimation(ContainerType.getSpecificAnimation(type) + ".open", false)
                    .addAnimation(ContainerType.getSpecificAnimation(type) + ".opened", true));
            isAnimOpen = true;
        }
        if(!this.getWorld().getBlockState(this.getPos()).get(GenericChestBlock.OPEN) && isAnimOpen == true) {
            controller.setAnimation(new AnimationBuilder().addAnimation(ContainerType.getSpecificAnimation(type) + ".close", false));
            isAnimOpen = false;
        }
        return PlayState.CONTINUE;
    }
    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<GenericChestEntity>(this, "controller", 0, this::predicate));
    }
    @Override
    public AnimationFactory getFactory() {
        return this.factory;
    }
    //endregion GeckoLib Animation
}
