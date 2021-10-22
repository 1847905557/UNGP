package data.scripts.ungprules.impl.other;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.intel.MessageIntel;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import data.scripts.campaign.UNGP_InGameData;
import data.scripts.campaign.everyframe.UNGP_CampaignPlugin.TempCampaignParams;
import data.scripts.campaign.specialist.rules.UNGP_RulesManager;
import data.scripts.campaign.specialist.rules.UNGP_RulesManager.URule;
import data.scripts.ungprules.impl.UNGP_BaseRuleEffect;
import data.scripts.ungprules.tags.UNGP_CampaignTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UNGP_EntropiusMind extends UNGP_BaseRuleEffect implements UNGP_CampaignTag {

    public static final String KEY_DAYS_PASSED = "$UNGP_EntropiusMind_DaysPassed";

    @Override
    public void updateDifficultyCache(int difficulty) {
    }

    @Override
    public float getValueByDifficulty(int index, int difficulty) {
        return 0f;
    }

    @Override
    public void advanceInCampaign(float amount, TempCampaignParams params) {

        if (params.isOneDayPassed()) {
            int daysPassed = (int) Global.getSector().getMemoryWithoutUpdate().get(KEY_DAYS_PASSED);
            daysPassed++;

            if (daysPassed == 7) {

                UNGP_InGameData inGameData = UNGP_InGameData.getDataInSave();
                List<URule> allRules = UNGP_RulesManager.getAllRulesCopy();
                List<URule> activatedRules = new ArrayList<>();
                activatedRules.add(rule);

                Random random = getRandomByDay();
                WeightedRandomPicker<URule> picker = new WeightedRandomPicker<>(random);
                for (URule otherRule : allRules) {
                    if (otherRule != rule && otherRule.isRollable()) {
                        picker.add(otherRule);
                    }
                }

                int rulesToAdd = random.nextInt((int) picker.getTotal());
                for (int i = 0; i < rulesToAdd; i++) {
                    URule toAdd = picker.pickAndRemove();
                    activatedRules.add(toAdd);
                }

                MessageIntel message = new MessageIntel(rule.getName(), rule.getCorrectColor());
                message.setIcon(rule.getSpritePath());
                message.addLine(rule.getExtra1(), Misc.getHighlightColor());
                showMessage(message);

                inGameData.saveActivatedRules(activatedRules);
                UNGP_RulesManager.updateCacheNextFrame();

                daysPassed = 0;
            }

            Global.getSector().getMemoryWithoutUpdate().set(KEY_DAYS_PASSED, daysPassed);
        }
    }

    @Override
    public void applyGlobalStats() {
        if (!Global.getSector().getMemoryWithoutUpdate().contains(KEY_DAYS_PASSED)) {
            Global.getSector().getMemoryWithoutUpdate().set(KEY_DAYS_PASSED, 0);
        }
    }

    @Override
    public void unapplyGlobalStats() {
    }

    @Override
    public String getDescriptionParams(int index, int difficulty) {
        return null;
    }
}
