/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart; // kept: constructor parameter
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

import static hellfirepvp.astralsorcery.AstralSorcery.MODID;

/**
 * Custom 3-D model for the Constellation Mantle chestplate.
 * Replaces the standard humanoid body/arm/head parts with shaped mesh parts.
 *
 * <p>1.16 → 1.20: {@code ModelRenderer} → {@code ModelPart};
 * {@code BipedModel} → {@code HumanoidModel} via {@code CustomArmorModel};
 * Static mesh defined via {@code LayerDefinition} / {@code MeshDefinition}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class ModelArmorMantle extends CustomArmorModel<LivingEntity> {

    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(new ResourceLocation(MODID, "armor_mantle"), "main");

    public ModelArmorMantle(@Nonnull ModelPart root) {
        super(root);
        // Child parts render automatically via the HumanoidModel tree traversal.
        // No manual field references needed.
    }

    // =========================================================================
    // Layer definition
    // =========================================================================

    /** Called during client setup to register the layer with Minecraft's model registry. */
    @Nonnull
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh  = new MeshDefinition();
        PartDefinition root  = mesh.getRoot();

        // Standard skeleton — empty boxes so they animate correctly but don't draw
        PartDefinition headDef     = root.addOrReplaceChild("head",
                CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("hat",
                CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition bodyDef     = root.addOrReplaceChild("body",
                CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition rightArmDef = root.addOrReplaceChild("right_arm",
                CubeListBuilder.create(), PartPose.offset(-5, 2, 0));
        PartDefinition leftArmDef  = root.addOrReplaceChild("left_arm",
                CubeListBuilder.create(), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg",
                CubeListBuilder.create(), PartPose.offset(-1.9f, 12, 0));
        root.addOrReplaceChild("left_leg",
                CubeListBuilder.create(), PartPose.offset(1.9f, 12, 0));

        float s = 0.01f; // inflate (thin shell)

        // ---- Head: cowl ----
        headDef.addOrReplaceChild("cowl",
                CubeListBuilder.create().texOffs(0, 33)
                        .addBox(-4.5f, -4.0f, -4.0f, 9, 5, 9, new CubeDeformation(s)),
                PartPose.offsetAndRotation(0, 0, 0, 0.2617993877991494f, 0, 0));

        // ---- Body: body_mesh → plate, mantle_l, mantle_r ----
        PartDefinition bodyMeshDef = bodyDef.addOrReplaceChild("body_mesh",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-4.5f, -0.5f, -3.0f, 9, 6, 6, new CubeDeformation(s)),
                PartPose.ZERO);

        bodyMeshDef.addOrReplaceChild("plate",
                CubeListBuilder.create().texOffs(0, 12)
                        .addBox(-3.5f, -0.5f, -1.0f, 7, 7, 2, new CubeDeformation(s)),
                PartPose.offsetAndRotation(0, 1, -3, 0.08726646259971647f, 0, 0));

        bodyMeshDef.addOrReplaceChild("mantle_l",
                CubeListBuilder.create().texOffs(0, 47).mirror()
                        .addBox(-8.0f, -3.5f, 1.0f, 9, 21, 5, new CubeDeformation(s)),
                PartPose.offsetAndRotation(6.25f, 2.0f, 0, 0.08726646259971647f, 0.2617993877991494f, 0));

        bodyMeshDef.addOrReplaceChild("mantle_r",
                CubeListBuilder.create().texOffs(0, 47)
                        .addBox(-1.0f, -3.5f, 1.0f, 9, 21, 5, new CubeDeformation(s)),
                PartPose.offsetAndRotation(-6.25f, 2.0f, 0, 0.08726646259971647f, -0.2617993877991494f, 0));

        // ---- Left arm: pauldron → fitting_l ----
        PartDefinition armLDef = leftArmDef.addOrReplaceChild("arm_l_pauldron",
                CubeListBuilder.create().texOffs(0, 21).mirror()
                        .addBox(-5.45f, -4.0f, -3.0f, 5, 6, 6, new CubeDeformation(s)),
                PartPose.ZERO);

        armLDef.addOrReplaceChild("fitting_l",
                CubeListBuilder.create().texOffs(18, 12)
                        .addBox(-6.0f, -2.0f, -1.0f, 4, 1, 2, new CubeDeformation(s)),
                PartPose.offsetAndRotation(0.5f, -3.0f, 0, 0, 0, 0.08726646259971647f));

        // ---- Right arm: pauldron → fitting_r ----
        PartDefinition armRDef = rightArmDef.addOrReplaceChild("arm_r_pauldron",
                CubeListBuilder.create().texOffs(0, 21)
                        .addBox(0.45f, -4.0f, -3.0f, 5, 6, 6, new CubeDeformation(s)),
                PartPose.ZERO);

        armRDef.addOrReplaceChild("fitting_r",
                CubeListBuilder.create().texOffs(18, 12)
                        .addBox(1.5f, -2.0f, -1.0f, 4, 1, 2, new CubeDeformation(s)),
                PartPose.offsetAndRotation(0, -3.0f, 0, 0, 0, -0.08726646259971647f));

        return LayerDefinition.create(mesh, 64, 128);
    }

    // =========================================================================
    // Render
    // =========================================================================

    @Override
    public void renderToBuffer(@Nonnull PoseStack poseStack, @Nonnull VertexConsumer buffer,
                                int packedLight, int packedOverlay,
                                float red, float green, float blue, float alpha) {
        // Show only our custom parts; hide helmet/legs
        hat.visible       = false;
        leftLeg.visible   = false;
        rightLeg.visible  = false;

        head.visible     = true;
        body.visible     = true;
        leftArm.visible  = true;
        rightArm.visible = true;

        super.renderToBuffer(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
