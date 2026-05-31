package hellfirepvp.astralsorcery.client.model.builtin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import hellfirepvp.astralsorcery.AstralSorcery;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Java model for the Attunement Altar block entity.
 * Ported from 1.16 ModelRenderer to 1.20.1 ModelPart/LayerDefinition.
 *
 * <p>Geometry: a flat marble base (20×6×20 model units) with 8 small orbiting
 * crystal cubes (4×4×4) that revolve around the center.</p>
 *
 * <p>Texture: {@code textures/block/entity/attunement_altar.png} (128×32)</p>
 */
@OnlyIn(Dist.CLIENT)
public class ModelAttunementAltar {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            AstralSorcery.key("attunement_altar"), "main");

    public static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/block/entity/attunement_altar.png");

    private final ModelPart base;
    private final ModelPart hovering;

    public ModelAttunementAltar(ModelPart root) {
        this.base     = root.getChild("base");
        this.hovering = root.getChild("hovering");
    }

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition partDefs = mesh.getRoot();

        partDefs.addOrReplaceChild("base",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-10f, -14f, -10f, 20, 6, 20, CubeDeformation.NONE),
                PartPose.offset(0f, 16f, 0f));

        partDefs.addOrReplaceChild("hovering",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(0f, 0f, 0f, 4, 4, 4, CubeDeformation.NONE),
                PartPose.offset(-2f, -16f, -2f));

        return LayerDefinition.create(mesh, 128, 32);
    }

    public void renderBase(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        base.render(poseStack, buffer, packedLight, packedOverlay);
    }

    public void renderHovering(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                                float offX, float offZ, float perc) {
        float distance = 0.9453125f;
        hovering.x = -2f + (16f * offX * distance);
        hovering.y = -16f;
        hovering.z = -2f + (16f * offZ * distance);
        hovering.xRot = offZ * 0.39269908f * perc;
        hovering.yRot = 0f;
        hovering.zRot = offX * -0.39269908f * perc;
        hovering.render(poseStack, buffer, packedLight, packedOverlay);
    }
}
