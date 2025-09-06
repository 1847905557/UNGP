package ungp.impl.rules.character;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import ungp.api.rules.UNGP_BaseRuleEffect;
import ungp.api.rules.tags.UNGP_PlayerCharacterStatsSkillTag;
import ungp.scripts.campaign.specialist.UNGP_SpecialistSettings;

public class UNGP_TechRevolution extends UNGP_BaseRuleEffect implements UNGP_PlayerCharacterStatsSkillTag {
    public static final float MAXIMUM_CYCLE = 4f;
    private float bonus;

    @Override
    public void updateDifficultyCache(UNGP_SpecialistSettings.Difficulty difficulty) {
        bonus = getValueByDifficulty(0, difficulty);
    }

    @Override
    public float getValueByDifficulty(int index, UNGP_SpecialistSettings.Difficulty difficulty) {
        if (index == 0) return difficulty.getLinearValue(0.08f, 0.02f);
        return super.getValueByDifficulty(index, difficulty);
    }

    @Override
    public void apply(MutableCharacterStatsAPI stats) {
        stats.getShipOrdnancePointBonus().modifyPercent(buffID, bonus * 100f * getBonusFactor());

        //        Global.getSector().getClock().getCycle();
//        FleetDataAPI playerFleetData = getFleetData(null);
//        if (playerFleetData != null) {
//            float totalFleetDP = getTotalCombatOP(playerFleetData, stats);
//            if (totalFleetDP > OP_ALL_THRESHOLD) {
//                opMult = Math.max(0.5f, 1f - 0.5f * ((totalFleetDP - OP_ALL_THRESHOLD) / (OP_ALL_THRESHOLD)));
//            }
//        }
//        stats.getShipOrdnancePointBonus().modifyPercent(buffID, bonus * 100f * opMult);
    }

    private float getBonusFactor() {
        Long latestTimestamp = getDataInCampaign(0);
        if (latestTimestamp == null) {
            // 生效、选取时的时间戳
            latestTimestamp = Global.getSector().getClock().getTimestamp();
            saveDataInCampaign(0, latestTimestamp);
        }
        // 按照与初次选取规则的时间差提高装配点
        long exTime = Math.max(0, Global.getSector().getClock().getTimestamp() - latestTimestamp);
        float cycleFactor = 0;
        if (exTime > 0) {
            cycleFactor = (float) exTime / 1000f / 60f / 60f / 24f / 365f;
            cycleFactor *= 1f/ MAXIMUM_CYCLE;
        }

        return Math.min(1f, 0.25f + 0.75f * cycleFactor);
    }

    @Override
    public void unapply(MutableCharacterStatsAPI stats) {
        stats.getShipOrdnancePointBonus().unmodify(buffID);
    }

    @Override
    public boolean addIntelTips(TooltipMakerAPI imageTooltip) {
        imageTooltip.addPara(rule.getExtra1(), 0f);
        imageTooltip.addPara((int)(getBonusFactor() * 100f) + "%", Misc.getHighlightColor(),0f);
        return super.addIntelTips(imageTooltip);
    }

    @Override
    public String getDescriptionParams(int index, UNGP_SpecialistSettings.Difficulty difficulty) {
        if (index == 0) return getPercentString(getValueByDifficulty(index, difficulty) * 100f);
        if (index == 1) return getPercentString(25f);
        if (index == 2) return (int)MAXIMUM_CYCLE + "";
        return super.getDescriptionParams(index, difficulty);
    }
}
