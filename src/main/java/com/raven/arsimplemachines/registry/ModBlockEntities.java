package com.raven.arsimplemachines.registry;

import com.raven.arsimplemachines.ArSimpleMachines;

import com.raven.arsimplemachines.blockentity.GasChargePadBlockEntity;
import com.raven.arsimplemachines.blockentity.LatheControllerBlockEntity;
import com.raven.arsimplemachines.blockentity.RollingControllerBlockEntity;
import com.raven.arsimplemachines.blockentity.ChemicalReactorControllerBlockEntity;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ArSimpleMachines.MODID);

    // -------------------------
    // GAS CHARGE PAD
    // -------------------------
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GasChargePadBlockEntity>> GAS_CHARGE_PAD_BE =
            BLOCK_ENTITIES.register("gas_charge_pad",
                    () -> BlockEntityType.Builder.of(
                            GasChargePadBlockEntity::new,
                            ModBlocks.GAS_CHARGE_PAD.get()
                    ).build(null));

    // -------------------------
    // LATHE CONTROLLER
    // -------------------------
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LatheControllerBlockEntity>> LATHE_CONTROLLER =
            BLOCK_ENTITIES.register("lathe_controller",
                    () -> BlockEntityType.Builder.of(
                            LatheControllerBlockEntity::new,
                            ModBlocks.LATHE_CONTROLLER.get()
                    ).build(null));

    // -------------------------
    // ROLLING CONTROLLER
    // -------------------------
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RollingControllerBlockEntity>> ROLLING_CONTROLLER =
            BLOCK_ENTITIES.register("rolling_controller",
                    () -> BlockEntityType.Builder.of(
                            RollingControllerBlockEntity::new,
                            ModBlocks.ROLLING_CONTROLLER.get()
                    ).build(null));

    // -------------------------
    // CHEMICAL REACTOR CONTROLLER
    // -------------------------
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ChemicalReactorControllerBlockEntity>> CHEMICAL_REACTOR_CONTROLLER =
            BLOCK_ENTITIES.register("chemical_reactor_controller",
                    () -> BlockEntityType.Builder.of(
                            ChemicalReactorControllerBlockEntity::new,
                            ModBlocks.CHEMICAL_REACTOR_CONTROLLER.get()
                    ).build(null));
}
