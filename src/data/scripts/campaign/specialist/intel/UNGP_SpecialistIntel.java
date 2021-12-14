package data.scripts.campaign.specialist.intel;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin;
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin;
import com.fs.starfarer.api.ui.*;
import com.fs.starfarer.api.util.Misc;
import data.scripts.campaign.UNGP_InGameData;
import data.scripts.campaign.specialist.UNGP_SpecialistSettings;
import data.scripts.campaign.specialist.challenges.UNGP_ChallengeManager;
import data.scripts.campaign.specialist.items.UNGP_RuleItem;
import data.scripts.campaign.specialist.rules.UNGP_RepickRulesDialog;
import data.scripts.campaign.specialist.rules.UNGP_RuleSorter;
import data.scripts.campaign.specialist.rules.UNGP_RulesManager;
import data.scripts.utils.UNGP_UIRect;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.IntelSortTier.TIER_0;
import static com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator;
import static com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation;
import static data.scripts.campaign.UNGP_Settings.d_i18n;
import static data.scripts.campaign.specialist.rules.UNGP_RulesManager.*;

public class UNGP_SpecialistIntel extends BaseIntelPlugin {
    private static final String KEY = "UNGP_SI";

    public static class RuleMessage {
        URule rule;
        String text;
        String[] highlights;

        public RuleMessage(URule rule, String text, String... highlights) {
            this.rule = rule;
            this.text = text;
            this.highlights = highlights;
        }

        public void send() {
            getInstance().sendUpdateIfPlayerHasIntel(this, false);
        }
    }

    public UNGP_SpecialistIntel() {
        this.setImportant(true);
    }

    public static UNGP_SpecialistIntel getInstance() {
        IntelInfoPlugin intel = Global.getSector().getIntelManager().getFirstIntel(UNGP_SpecialistIntel.class);
        if (intel == null) {
            intel = new UNGP_SpecialistIntel();
            Global.getSector().getIntelManager().addIntel(intel);
        }
        return (UNGP_SpecialistIntel) intel;
    }

    private static final Object OPTION_ID_DETAILS = new Object();
    private static final Object OPTION_ID_TIPS = new Object();
    private static Object checkedButton = OPTION_ID_DETAILS;

    @Override
    public void notifyPlayerAboutToOpenIntelScreen() {
        UNGP_SpecialistBackgroundUI.cleanBGUI();
        checkedButton = OPTION_ID_DETAILS;
    }

    @Override
    public void createLargeDescription(CustomPanelAPI panel, float width, float height) {
        UNGP_SpecialistBackgroundUI.resumeTicking();
        CustomPanelAPI customPanel = panel.createCustomPanel(width, height, UNGP_SpecialistBackgroundUI.getInstance());
        panel.addComponent(customPanel);
        // 数据处理
        TooltipMakerAPI tooltip;
        Color positiveColor = Misc.getHighlightColor();
        boolean showDetails = checkedButton == OPTION_ID_DETAILS;

        UNGP_InGameData inGameData = UNGP_InGameData.getDataInSave();
        List<URule> bonusRules = new ArrayList<>();
        List<URule> notBonusRules = new ArrayList<>();
        for (URule rule : ACTIVATED_RULES_IN_THIS_GAME) {
            if (rule.isBonus()) {
                bonusRules.add(rule);
            } else {
                notBonusRules.add(rule);
            }
        }
        Collections.sort(bonusRules, new UNGP_RuleSorter());
        Collections.sort(notBonusRules, new UNGP_RuleSorter());

        // 状态
        List<URule> combatRules = new ArrayList<>();
        List<URule> campaignRules = new ArrayList<>();
        for (URule rule : bonusRules) {
            if (CAMPAIGN_RULES_IN_THIS_GAME.contains(rule)) {
                campaignRules.add(rule);
            }
            if (COMBAT_RULES_IN_THIS_GAME.contains(rule)) {
                combatRules.add(rule);
            }
        }
        for (URule rule : notBonusRules) {
            if (CAMPAIGN_RULES_IN_THIS_GAME.contains(rule)) {
                campaignRules.add(rule);
            }
            if (COMBAT_RULES_IN_THIS_GAME.contains(rule)) {
                combatRules.add(rule);
            }
        }

        // UI生成
        float contentShrink = 20f;
        UNGP_UIRect fullScreen = new UNGP_UIRect(0, 0, width, height);
        UNGP_UIRect[] fullScreenSplits = fullScreen.splitVertically(120f);
        UNGP_UIRect titleRect = fullScreenSplits[0];
        // title
        {
            UNGP_UIRect[] titleRectSplits = titleRect.splitHorizontally(0.35f, 0.35f, 0.3f);
            UNGP_UIRect levelTitle = titleRectSplits[0].shrink(contentShrink);
            {
                tooltip = levelTitle.beginTooltip(panel, false);
                TooltipMakerAPI imageMaker = tooltip.beginImageWithText(UNGP_SpecialistSettings.getSpecialistModeIconPath(), 80f);
                imageMaker.setParaOrbitronLarge();
                imageMaker.addPara(d_i18n.get("rulepick_level"), 0, positiveColor, inGameData.getDifficultyLevel() + "");
                imageMaker.setParaFontDefault();
                tooltip.addImageWithText(0f);
                levelTitle.addTooltip();
            }
            UNGP_UIRect checkBoxRect = titleRectSplits[1].shrink(contentShrink);
            {
                tooltip = checkBoxRect.beginTooltip(panel, false);
                final float checkBoxRectWidth = checkBoxRect.getWidth();
                final float checkBoxRectHeight = checkBoxRect.getHeight();
                ButtonAPI button_details = tooltip.addAreaCheckbox(rules_i18n.get("button_details"), OPTION_ID_DETAILS,
                                                                   Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                                                                   checkBoxRectWidth, checkBoxRectHeight * 0.4f, 0f);
                tooltip.addTooltipToPrevious(new TooltipCreator() {
                    @Override
                    public boolean isTooltipExpandable(Object tooltipParam) {
                        return false;
                    }

                    @Override
                    public float getTooltipWidth(Object tooltipParam) {
                        return checkBoxRectWidth * 0.4f;
                    }

                    @Override
                    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                        tooltip.addPara(rules_i18n.get("button_details_tooltip"), 0f);
                    }
                }, TooltipLocation.LEFT);
                button_details.setChecked(checkedButton == OPTION_ID_DETAILS);
                ButtonAPI button_tips = tooltip.addAreaCheckbox(rules_i18n.get("button_tips"), OPTION_ID_TIPS,
                                                                Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(),
                                                                checkBoxRectWidth, checkBoxRectHeight * 0.4f, checkBoxRectHeight * 0.2f);
                tooltip.addTooltipToPrevious(new TooltipCreator() {
                    @Override
                    public boolean isTooltipExpandable(Object tooltipParam) {
                        return false;
                    }

                    @Override
                    public float getTooltipWidth(Object tooltipParam) {
                        return checkBoxRectWidth * 0.4f;
                    }

                    @Override
                    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded, Object tooltipParam) {
                        tooltip.addPara(rules_i18n.get("button_tips_tooltip"), 0f);
                    }
                }, TooltipLocation.LEFT);
                button_tips.setChecked(checkedButton == OPTION_ID_TIPS);
                checkBoxRect.addTooltip();
            }
            UNGP_UIRect repickRect = titleRectSplits[2].shrink(contentShrink);
            {
                tooltip = repickRect.beginTooltip(panel, false);
                tooltip.addPara(rules_i18n.get("current_cycle") + "%s", 0f, positiveColor, Global.getSector().getClock().getCycle() + "").setAlignment(Alignment.RMID);
                boolean lockedBecauseOfChallenges = UNGP_ChallengeManager.isRepickLockedByChallenges();
                if (lockedBecauseOfChallenges) {
                    tooltip.addPara(rules_i18n.get("repick_blocked"), Misc.getNegativeHighlightColor(), 0f).setAlignment(Alignment.RMID);
                } else {
                    tooltip.addPara(rules_i18n.get("current_times_to_refresh") + "%s", 0f, positiveColor, rules_i18n.get("repick_rules"), inGameData.getTimesToChangeSpecialistMode() + "").setAlignment(Alignment.RMID);
                }
                Color buttonBase = Misc.getBasePlayerColor();
                Color buttonDark = Misc.getDarkPlayerColor();
                ButtonAPI button = tooltip.addButton(rules_i18n.get("repick_rules"), KEY, buttonBase, buttonDark, Alignment.MID, CutStyle.C2_MENU, repickRect.getWidth(), repickRect.getHeight() * 0.35f, 20f);
                // 设置重选
                button.setEnabled(!lockedBecauseOfChallenges && inGameData.getTimesToChangeSpecialistMode() > 0);
                repickRect.addTooltip();
            }
        }


        // content
        UNGP_UIRect contentRect = fullScreenSplits[1];
        {
            UNGP_UIRect[] contentRectSplits = contentRect.splitHorizontally(0.35f, 0.35f, 0.3f);
            UNGP_UIRect positiveRect = contentRectSplits[0];
            {
                UNGP_UIRect[] positiveRectSplits = positiveRect.splitVertically(30f);
                UNGP_UIRect positiveRectTitle = positiveRectSplits[0].shrink(contentShrink);
                {
                    // 正面规则标题
                    tooltip = positiveRectTitle.beginTooltip(panel, false);
                    tooltip.setParaOrbitronLarge();
                    tooltip.addPara(UNGP_RulesManager.getBonusString(true), UNGP_RulesManager.getBonusColor(true), 0f);
                    tooltip.setParaFontDefault();
                    addLine(tooltip, positiveRectTitle.getWidth() - 5f, 1f, 3f);
                    positiveRectTitle.addTooltip();
                }
                UNGP_UIRect positiveRectContent = positiveRectSplits[1].shrink(contentShrink);
                {
                    //正面规则内容
                    tooltip = positiveRectContent.beginTooltip(panel, true);
                    for (URule rule : bonusRules) {
                        TooltipMakerAPI iconMaker = tooltip.beginImageWithText(rule.getSpritePath(), 64f);
                        iconMaker.addPara(rule.getName(), rule.getCorrectColor(), 0f);
                        if (showDetails) {
                            rule.addDesc(iconMaker, 0f);
                            tooltip.addImageWithText(10f);
                            tooltip.addTooltipToPrevious(UNGP_RuleItem.createRuleItemTooltip(rule), TooltipLocation.BELOW);
                        } else if (rule.getRuleEffect().addIntelTips(iconMaker)) {
                            tooltip.addImageWithText(10f);
                        }
                    }
                    positiveRectContent.addTooltip();
                }
            }
            UNGP_UIRect negativeRect = contentRectSplits[1];
            {
                UNGP_UIRect[] negativeRectSplits = negativeRect.splitVertically(30f);
                UNGP_UIRect negativeRectTitle = negativeRectSplits[0].shrink(contentShrink);
                {
                    // 负面规则标题
                    tooltip = negativeRectTitle.beginTooltip(panel, false);
                    tooltip.setParaOrbitronLarge();
                    tooltip.addPara(UNGP_RulesManager.getBonusString(false), UNGP_RulesManager.getBonusColor(false), 0f);
                    tooltip.setParaFontDefault();
                    addLine(tooltip, negativeRectTitle.getWidth() - 5f, 1f, 3f);
                    negativeRectTitle.addTooltip();
                }
                UNGP_UIRect negativeRectContent = negativeRectSplits[1].shrink(contentShrink);
                {
                    //负面规则内容
                    tooltip = negativeRectContent.beginTooltip(panel, true);
                    for (URule rule : notBonusRules) {
                        TooltipMakerAPI iconMaker = tooltip.beginImageWithText(rule.getSpritePath(), 64f);
                        iconMaker.addPara(rule.getName(), rule.getCorrectColor(), 0f);
                        if (showDetails) {
                            rule.addDesc(iconMaker, 0f);
                            tooltip.addImageWithText(10f);
                            tooltip.addTooltipToPrevious(UNGP_RuleItem.createRuleItemTooltip(rule), TooltipLocation.BELOW);
                        } else if (rule.getRuleEffect().addIntelTips(iconMaker)) {
                            tooltip.addImageWithText(10f);
                        }
                    }
                    negativeRectContent.addTooltip();
                }
            }
            UNGP_UIRect gameStateRect = contentRectSplits[2];
            {
                UNGP_UIRect[] gameStateRectSplits = gameStateRect.splitVertically(30f);
                UNGP_UIRect gameStateRectTitle = gameStateRectSplits[0].shrink(contentShrink);
                {
                    // 状态标题
                    tooltip = gameStateRectTitle.beginTooltip(panel, false);
                    tooltip.setParaOrbitronLarge();
                    tooltip.addPara(rules_i18n.get("suited_state"), Misc.getButtonTextColor(), 0f);
                    tooltip.setParaFontDefault();
                    addLine(tooltip, gameStateRectTitle.getWidth() - 5f, 1f, 3f);
                    gameStateRectTitle.addTooltip();
                }
                UNGP_UIRect gameStateRectContent = gameStateRectSplits[1].shrink(contentShrink);
                {
                    tooltip = gameStateRectContent.beginTooltip(panel, true);
                    tooltip.addSpacer(5f);
                    if (!campaignRules.isEmpty()) {
                        tooltip.setParaOrbitronLarge();
                        tooltip.addPara(rules_i18n.get("campaign_state"), 0f);
                        tooltip.setParaFontDefault();
                        tooltip.addSpacer(5f);
                        tooltip.setBulletedListMode("    ");
                        for (URule rule : campaignRules) {
                            tooltip.addPara(rule.getName(), rule.getCorrectColor(), 3f);
                        }
                        tooltip.setBulletedListMode(null);
                    }
                    if (!combatRules.isEmpty()) {
                        tooltip.setParaOrbitronLarge();
                        tooltip.addPara(rules_i18n.get("combat_state"), 10f);
                        tooltip.setParaFontDefault();
                        tooltip.addSpacer(5f);
                        tooltip.setBulletedListMode("    ");
                        for (URule rule : combatRules) {
                            tooltip.addPara(rule.getName(), rule.getCorrectColor(), 3f);
                        }
                        tooltip.setBulletedListMode(null);
                    }
                    gameStateRectContent.addTooltip();
                }
            }
        }
    }

    private void addLine(TooltipMakerAPI tooltip, float width, float height, float pad) {
        ButtonAPI button = tooltip.addButton("", new Object(), width, height, pad);
        button.setEnabled(false);
        button.setButtonDisabledPressedSound(null);
        button.setButtonPressedSound(null);
        button.setMouseOverSound(null);
        button.setChecked(true);
    }

    @Override
    public void buttonPressConfirmed(Object buttonId, IntelUIAPI ui) {
        if (buttonId == KEY) {
            ui.showDialog(null, new UNGP_RepickRulesDialog(ui, this));
            UNGP_SpecialistBackgroundUI.stopTicking();
        }
        if (buttonId == OPTION_ID_DETAILS || buttonId == OPTION_ID_TIPS) {
            checkedButton = buttonId;
            ui.updateUIForItem(this);
        }
    }


    @Override
    public void createIntelInfo(TooltipMakerAPI info, ListInfoMode mode) {
        Color h = Misc.getHighlightColor();
        Color g = Misc.getGrayColor();
        Color c = getTitleColor(mode);
        float pad = 3f;
        float opad = 10f;

        if (listInfoParam == null) {
            info.addPara(getName(), c, 0f);
            bullet(info);
            info.addPara(getDesc(), g, pad);
        } else {
            RuleMessage message = (RuleMessage) listInfoParam;
            info.addPara(message.rule.getName(), c, 0f);
            bullet(info);
            info.addPara(message.text, pad, g, h, message.highlights);
        }
        unindent(info);
    }

    public String getDesc() {
        return rules_i18n.get("mode_desc");
    }

    public String getName() {
        return rules_i18n.get("mode_name");
    }

    @Override
    public String getIcon() {
        if (listInfoParam == null) {
            return Global.getSettings().getSpriteName("icons", "UNGP_hmlogo");
        } else {
            RuleMessage message = (RuleMessage) listInfoParam;
            return message.rule.getSpritePath();
        }
    }

    @Override
    public Set<String> getIntelTags(SectorMapAPI map) {
        Set<String> tags = super.getIntelTags(map);
        tags.add("ungp");
        return tags;
    }

    @Override
    public String getCommMessageSound() {
        if (isSendingUpdate()) {
            return getSoundStandardUpdate();
        }
        return "ui_specialist_on";
    }


    @Override
    public IntelSortTier getSortTier() {
        return TIER_0;
    }

    @Override
    public boolean hasSmallDescription() {
        return false;
    }

    @Override
    public boolean hasLargeDescription() {
        return true;
    }
}
