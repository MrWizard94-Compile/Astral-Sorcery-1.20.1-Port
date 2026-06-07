/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2024
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/
package hellfirepvp.astralsorcery.client.model.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Base class for Astral Sorcery custom armor models.
 * Handles armor-stand pose propagation for correct preview rendering.
 *
 * <p>1.16 → 1.20: {@code BipedModel<T>} → {@code HumanoidModel<T>};
 * {@code ModelRenderer} → {@code ModelPart};
 * {@code setRotationAngles} → {@code setupAnim};
 * {@code bipedHead/Body/…} → {@code head/body/…};
 * {@code rotateAngleX/Y/Z} → {@code xRot/yRot/zRot}.</p>
 */
@OnlyIn(Dist.CLIENT)
public class CustomArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

    public CustomArmorModel(@Nonnull ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(@Nonnull T entity, float limbSwing, float limbSwingAmount,
                           float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof ArmorStand stand) {
            float r = (float) (Math.PI / 180.0);

            head.xRot = r * stand.getHeadPose().getX();
            head.yRot = r * stand.getHeadPose().getY();
            head.zRot = r * stand.getHeadPose().getZ();

            body.xRot = r * stand.getBodyPose().getX();
            body.yRot = r * stand.getBodyPose().getY();
            body.zRot = r * stand.getBodyPose().getZ();

            leftArm.xRot  = r * stand.getLeftArmPose().getX();
            leftArm.yRot  = r * stand.getLeftArmPose().getY();
            leftArm.zRot  = r * stand.getLeftArmPose().getZ();

            rightArm.xRot = r * stand.getRightArmPose().getX();
            rightArm.yRot = r * stand.getRightArmPose().getY();
            rightArm.zRot = r * stand.getRightArmPose().getZ();

            leftLeg.xRot  = r * stand.getLeftLegPose().getX();
            leftLeg.yRot  = r * stand.getLeftLegPose().getY();
            leftLeg.zRot  = r * stand.getLeftLegPose().getZ();

            rightLeg.xRot = r * stand.getRightLegPose().getX();
            rightLeg.yRot = r * stand.getRightLegPose().getY();
            rightLeg.zRot = r * stand.getRightLegPose().getZ();

            hat.copyFrom(head);
        } else {
            super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
