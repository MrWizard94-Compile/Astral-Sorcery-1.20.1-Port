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
 * Java model for the Observatory block entity.
 * Ported from 1.16 ModelRenderer to 1.20.1 ModelPart/LayerDefinition.
 *
 * <p>Hierarchy:
 * <ul>
 *   <li>base (+ base1..base8 children)</li>
 *   <li>seat (+ seat1..seat14 children, + tube child)</li>
 *   <li>tube (+ tube1..tube15 children) — child of seat</li>
 * </ul></p>
 *
 * <p>Texture: {@code textures/block/entity/observatory.png} (256×128)</p>
 */
@OnlyIn(Dist.CLIENT)
public class ModelObservatory {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            AstralSorcery.key("observatory"), "main");

    public static final ResourceLocation TEXTURE =
            AstralSorcery.key("textures/block/entity/observatory.png");

    private final ModelPart base;
    private final ModelPart seat;
    private final ModelPart tube;

    public ModelObservatory(ModelPart root) {
        this.base = root.getChild("base");
        this.seat = root.getChild("seat");
        this.tube = this.seat.getChild("tube");
    }

    public static LayerDefinition createLayerDefinition() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // ---- Base ----
        PartDefinition base = root.addOrReplaceChild("base",
                CubeListBuilder.create().texOffs(0, 82)
                        .addBox(-12f, 18f, -16f, 24, 6, 28, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));

        base.addOrReplaceChild("base1",
                CubeListBuilder.create().texOffs(120, 82)
                        .addBox(-14f, 4f, -18f, 6, 18, 12, CubeDeformation.NONE),
                PartPose.ZERO);
        base.addOrReplaceChild("base2",
                CubeListBuilder.create().texOffs(224, 52)
                        .addBox(-7f, 4f, -18f, 2, 18, 12, CubeDeformation.NONE),
                PartPose.ZERO);
        base.addOrReplaceChild("base3",
                CubeListBuilder.create().texOffs(224, 52)
                        .addBox(-4f, 4f, -18f, 2, 18, 12, CubeDeformation.NONE),
                PartPose.ZERO);
        base.addOrReplaceChild("base4",
                CubeListBuilder.create().texOffs(224, 52)
                        .addBox(-1f, 4f, -18f, 2, 18, 12, CubeDeformation.NONE),
                PartPose.ZERO);
        base.addOrReplaceChild("base5",
                CubeListBuilder.create().texOffs(180, 52)
                        .addBox(2f, 4f, -18f, 10, 18, 12, CubeDeformation.NONE),
                PartPose.ZERO);
        base.addOrReplaceChild("base6",
                CubeListBuilder.create().texOffs(192, 0)
                        .addBox(12f, -18f, -18f, 8, 40, 12, CubeDeformation.NONE),
                PartPose.ZERO);
        base.addOrReplaceChild("base7",
                CubeListBuilder.create().texOffs(156, 82)
                        .addBox(8f, 4f, -6f, 8, 18, 20, CubeDeformation.NONE),
                PartPose.ZERO);
        base.addOrReplaceChild("base8",
                CubeListBuilder.create().texOffs(192, 82)
                        .addBox(-8f, 28f, -8f, 16, 4, 16, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));

        // ---- Seat ----
        PartDefinition seat = root.addOrReplaceChild("seat",
                CubeListBuilder.create().texOffs(144, 28)
                        .addBox(-9f, 16f, 6f, 12, 4, 10, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));

        seat.addOrReplaceChild("seat1",
                CubeListBuilder.create().texOffs(144, 42)
                        .addBox(-9f, 16f, 0f, 12, 2, 4, CubeDeformation.NONE),
                PartPose.ZERO);
        seat.addOrReplaceChild("seat2",
                CubeListBuilder.create().texOffs(144, 10)
                        .addBox(-9f, 6f, 16f, 12, 14, 4, CubeDeformation.NONE),
                PartPose.ZERO);
        seat.addOrReplaceChild("seat3",
                CubeListBuilder.create().texOffs(144, 0)
                        .addBox(-7f, 2f, 16f, 8, 6, 4, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat4",
                CubeListBuilder.create().texOffs(140, 82)
                        .addBox(-1f, 18f, 12f, 2, 4, 8, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat5",
                CubeListBuilder.create().texOffs(156, 82)
                        .addBox(-1f, 22f, 12f, 2, 4, 8, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat6",
                CubeListBuilder.create().texOffs(156, 82)
                        .addBox(-7f, 22f, 12f, 2, 4, 8, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat7",
                CubeListBuilder.create().texOffs(232, 0)
                        .addBox(-1f, -2f, 20f, 2, 28, 2, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat8",
                CubeListBuilder.create().texOffs(232, 0)
                        .addBox(-7f, -2f, 20f, 2, 28, 2, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat9",
                CubeListBuilder.create().texOffs(232, 2)
                        .addBox(2f, -2f, 20f, 2, 22, 2, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat10",
                CubeListBuilder.create().texOffs(232, 30)
                        .addBox(-4f, -4f, 20f, 2, 20, 2, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat11",
                CubeListBuilder.create().texOffs(232, 2)
                        .addBox(-10f, -2f, 20f, 2, 22, 2, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat12",
                CubeListBuilder.create().texOffs(240, 0)
                        .addBox(2f, -6f, 20f, 2, 4, 4, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat13",
                CubeListBuilder.create().texOffs(240, 0)
                        .addBox(-4f, -8f, 20f, 2, 4, 4, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));
        seat.addOrReplaceChild("seat14",
                CubeListBuilder.create().texOffs(240, 0)
                        .addBox(-10f, -6f, 20f, 2, 4, 4, CubeDeformation.NONE),
                PartPose.offset(0f, -4f, 0f));

        // ---- Tube (child of seat) ----
        PartDefinition tube = seat.addOrReplaceChild("tube",
                CubeListBuilder.create().texOffs(0, 32)
                        .addBox(-2f, -4f, -4f, 14, 8, 8, CubeDeformation.NONE),
                PartPose.offsetAndRotation(0f, -12f, -12f, -0.7853982f, 0f, 0f));

        tube.addOrReplaceChild("tube1",
                CubeListBuilder.create().texOffs(92, 0)
                        .addBox(-2f, -4f, -36f, 14, 6, 6, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube2",
                CubeListBuilder.create().texOffs(78, 90)
                        .addBox(2f, -2f, -30f, 2, 2, 26, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube3",
                CubeListBuilder.create().texOffs(78, 90)
                        .addBox(6f, -2f, -30f, 2, 2, 26, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube4",
                CubeListBuilder.create().texOffs(92, 28)
                        .addBox(-2f, -16f, -2f, 14, 8, 2, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube5",
                CubeListBuilder.create().texOffs(92, 12)
                        .addBox(-2f, -7f, -2f, 14, 2, 2, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube6",
                CubeListBuilder.create().texOffs(92, 12)
                        .addBox(-2f, -16f, -34f, 14, 8, 2, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube7",
                CubeListBuilder.create().texOffs(92, 12)
                        .addBox(-2f, -7f, -34f, 14, 2, 2, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube8",
                CubeListBuilder.create().texOffs(92, 12)
                        .addBox(-2f, -16f, -40f, 14, 14, 2, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube9",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-2f, -16f, -60f, 14, 14, 18, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube10",
                CubeListBuilder.create().texOffs(0, 0)
                        .addBox(0f, -14f, -56f, 10, 10, 68, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube11",
                CubeListBuilder.create().texOffs(92, 50)
                        .addBox(-4f, -10f, 2f, 4, 4, 12, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube12",
                CubeListBuilder.create().texOffs(44, 32)
                        .addBox(-4f, -10f, 14f, 2, 2, 6, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube13",
                CubeListBuilder.create().texOffs(0, 48)
                        .addBox(2f, -12f, 12f, 6, 6, 4, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube14",
                CubeListBuilder.create().texOffs(92, 0)
                        .addBox(6f, -18f, -44f, 2, 2, 48, CubeDeformation.NONE),
                PartPose.ZERO);
        tube.addOrReplaceChild("tube15",
                CubeListBuilder.create().texOffs(92, 0)
                        .addBox(2f, -18f, -44f, 2, 2, 48, CubeDeformation.NONE),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 256, 128);
    }

    public void render(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay) {
        seat.render(poseStack, buffer, packedLight, packedOverlay);
        base.render(poseStack, buffer, packedLight, packedOverlay);
    }

    public void setupRotations(float yawDegrees, float pitchDegrees) {
        float yawRad   = (float) Math.toRadians(yawDegrees);
        float pitchRad = (float) Math.toRadians(pitchDegrees);
        seat.yRot = yawRad;
        base.yRot = yawRad;
        tube.xRot = pitchRad;
    }
}
