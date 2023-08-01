package giselle.jei_mekanism_multiblocks.client.jei.category;

import java.util.function.Consumer;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.gui.LabelWidget;
import giselle.jei_mekanism_multiblocks.client.gui.Mod2IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.jei.CostWidget;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import giselle.jei_mekanism_multiblocks.common.util.VolumeUnit;
import mekanism.api.math.FloatingLong;
import mekanism.api.math.MathUtils;
import mekanism.common.config.MekanismConfig;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.generators.common.config.MekanismGeneratorsConfig;
import mekanism.generators.common.content.turbine.TurbineMultiblockData;
import mekanism.generators.common.content.turbine.TurbineValidator;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mekanism.generators.common.registries.GeneratorsItems;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class IndustrialTurbineCategory extends MultiblockCategory<IndustrialTurbineCategory.IndustrialTurbineWidget>
{
	public IndustrialTurbineCategory(IGuiHelper helper)
	{
		super(helper, "industrial_turbine", null);
	}

	@Override
	protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer)
	{
		super.getRecipeCatalystItemStacks(consumer);
		consumer.accept(GeneratorsBlocks.TURBINE_CASING.getItemStack());
		consumer.accept(GeneratorsBlocks.TURBINE_VALVE.getItemStack());
		consumer.accept(GeneratorsBlocks.TURBINE_VENT.getItemStack());
		consumer.accept(GeneratorsBlocks.ROTATIONAL_COMPLEX.getItemStack());
		consumer.accept(GeneratorsBlocks.TURBINE_ROTOR.getItemStack());
		consumer.accept(GeneratorsItems.TURBINE_BLADE.getItemStack());
		consumer.accept(MekanismBlocks.PRESSURE_DISPERSER.getItemStack());
		consumer.accept(GeneratorsBlocks.ELECTROMAGNETIC_COIL.getItemStack());
		consumer.accept(GeneratorsBlocks.SATURATING_CONDENSER.getItemStack());
		consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());
	}

	@Override
	public void setIngredients(IndustrialTurbineWidget widget, IIngredients ingredients)
	{

	}

	@Override
	public Class<? extends IndustrialTurbineWidget> getRecipeClass()
	{
		return IndustrialTurbineWidget.class;
	}

	public static class IndustrialTurbineWidget extends MultiblockWidget
	{
		protected CheckBoxWidget useStructuralGlassCheckBox;
		protected IntSliderWithButtons rotorsWidget;
		protected IntSliderWithButtons ventsWidget;
		protected IntSliderWithButtons condensersWidget;
		protected IntSliderWithButtons valvesWidget;

		private boolean needMoreVents;

		public IndustrialTurbineWidget()
		{
			this.widthWidget.setTranslationKey("text.jei_mekanism_multiblocks.specs.dimensions.width_length");
		}

		@Override
		protected IntSliderWidget createDimensionSlider(int index, int min, int max)
		{
			if (index == 0)
			{
				return new Mod2IntSliderWidget(0, 0, 0, 0, StringTextComponent.EMPTY, min, min, max, 0);
			}

			return super.createDimensionSlider(index, min, max);
		}

		@Override
		protected boolean isUseDimensionWIidget(IntSliderWithButtons widget)
		{
			if (widget == this.lengthWidget)
			{
				return false;
			}

			return super.isUseDimensionWIidget(widget);
		}

		@Override
		protected void collectOtherConfigs(Consumer<Widget> consumer)
		{
			super.collectOtherConfigs(consumer);

			consumer.accept(this.useStructuralGlassCheckBox = new CheckBoxWidget(0, 0, 0, 0, new TranslationTextComponent("text.jei_mekanism_multiblocks.specs.use_things", MekanismBlocks.STRUCTURAL_GLASS.getItemStack().getHoverName()), true));
			this.useStructuralGlassCheckBox.addSelectedChangedHandler(this::onUseStructuralGlassChanged);
			consumer.accept(this.rotorsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.rotors", 0, 1, 0));
			this.rotorsWidget.getSlider().addIntValueChangeHanlder(this::onRotorsChanged);
			consumer.accept(this.ventsWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.vents", 0, 1, 0));
			this.ventsWidget.getSlider().addIntValueChangeHanlder(this::onVentsChanged);
			consumer.accept(this.condensersWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.condensers", 0, 0, 0));
			this.condensersWidget.getSlider().addIntValueChangeHanlder(this::onCondensersChanged);
			consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 2, 0));
			this.valvesWidget.getSlider().addIntValueChangeHanlder(this::onValvesChanged);

			this.updateRotorsSliderLimit();
		}

		@Override
		protected void onDimensionWidthChanged(int width)
		{
			width += width % 2 - 1;
			IntSliderWidget widthSlider = this.widthWidget.getSlider();
			widthSlider.setIntValue(width);

			super.onDimensionWidthChanged(width);

			IntSliderWidget lengthSlider = this.lengthWidget.getSlider();
			lengthSlider.setIntMinValue(width);
			lengthSlider.setIntMaxValue(width);
			lengthSlider.setIntValue(width);
		}

		@Override
		protected void onDimensionChanged()
		{
			super.onDimensionChanged();

			this.updateRotorsSliderLimit();
			this.setRotorCount(this.rotorsWidget.getSlider().getIntMaxValue());
			this.setVentCount(this.getClampedMaxVentCount(this.getRotorCount()));
			this.setCondenserCount(this.getClampedMaxCondenserCount(this.getRotorCount(), this.getVentCount()));
		}

		public void updateRotorsSliderLimit()
		{
			Vector3i inner = this.getDimensionInner();
			int innerRadius = (inner.getX() - 1) / 2;

			IntSliderWidget rotorsSlider = this.rotorsWidget.getSlider();
			int rotors = rotorsSlider.getIntValue();
			rotorsSlider.setIntMaxValue(Math.min((innerRadius + 1) * 4 - 3, inner.getY() - 2));
			rotorsSlider.setIntValue(rotors);

			this.updateVentsSliderLimit();
		}

		protected void onRotorsChanged(int rotors)
		{
			this.updateVentsSliderLimit();
			this.setVentCount(this.getClampedMaxVentCount(rotors));
			this.setCondenserCount(this.getClampedMaxCondenserCount(rotors, this.getVentCount()));
			this.markNeedUpdate();
		}

		public void updateVentsSliderLimit()
		{
			IntSliderWidget ventsSlider = this.ventsWidget.getSlider();
			int vents = ventsSlider.getIntValue();
			ventsSlider.setIntMaxValue(this.getClampedMaxVentCount(this.getRotorCount()));
			ventsSlider.setIntValue(vents);

			this.updateCondensersSliderLimit();
			this.updateValvesSliderLimit();
		}

		protected void onVentsChanged(int vents)
		{
			this.needMoreVents = vents < this.getClampedMaxVentCount(this.getRotorCount());
			this.updateCondensersSliderLimit();
			this.updateValvesSliderLimit();
			this.markNeedUpdate();
		}

		public void updateCondensersSliderLimit()
		{
			IntSliderWidget condensersSlider = this.condensersWidget.getSlider();
			int condensers = condensersSlider.getIntValue();
			condensersSlider.setIntMaxValue(this.getClampedMaxCondenserCount(this.getRotorCount(), this.getVentCount()));
			condensersSlider.setIntValue(condensers);
		}

		protected void onCondensersChanged(int condensers)
		{
			this.markNeedUpdate();
		}

		public void updateValvesSliderLimit()
		{
			IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
			int valves = valvesSlider.getIntValue();
			valvesSlider.setIntMaxValue(this.getSideBlocks() - this.getVentCount());
			valvesSlider.setIntValue(valves);
		}

		protected void onValvesChanged(int valves)
		{
			this.markNeedUpdate();
		}

		protected void onUseStructuralGlassChanged(boolean useStructuralGlass)
		{
			this.markNeedUpdate();
		}

		@Override
		public void collectCost(ICostConsumer consumer)
		{
			super.collectCost(consumer);

			int corners = this.getCornerBlocks();
			int sides = this.getSideBlocks();
			int rotors = this.getRotorCount();
			int blades = this.getBladeCount(rotors);
			int lowerVolume = this.getLowerVolume(rotors);

			int vents = this.getVentCount();
			sides -= vents;

			int valves = this.getValveCount();
			sides -= valves;

			int casing = 0;
			int structuralGlasses = 0;

			if (this.isUseStruturalGlass())
			{
				casing = corners;
				structuralGlasses = sides;
			}
			else
			{
				casing = corners + sides;
				structuralGlasses = 0;
			}

			int maxFlow = MathUtils.clampToInt(this.getMaxFlowRateClamped(lowerVolume, vents));

			consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_CASING, casing));
			consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_VALVE, valves));
			CostWidget vent = consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_VENT, vents));

			if (this.needMoreVents)
			{
				vent.setFGColor(0xFF8000);
				vent.setHeadTooltips(//
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.value_limited", new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_flow_rate")).withStyle(TextFormatting.RED), //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.TURBINE_VENT.getTextComponent()).withStyle(TextFormatting.RED));
			}

			consumer.accept(new ItemStack(GeneratorsBlocks.ROTATIONAL_COMPLEX));
			consumer.accept(new ItemStack(GeneratorsBlocks.TURBINE_ROTOR, rotors));
			consumer.accept(new ItemStack(GeneratorsItems.TURBINE_BLADE, blades));
			consumer.accept(new ItemStack(MekanismBlocks.PRESSURE_DISPERSER, this.getDisperserCount()));
			consumer.accept(new ItemStack(GeneratorsBlocks.ELECTROMAGNETIC_COIL, this.getNeededCoilCount(blades)));
			CostWidget maxWaterOutputWidget = consumer.accept(new ItemStack(GeneratorsBlocks.SATURATING_CONDENSER, this.getCondenserCount()));

			if (maxFlow > this.getMaxWaterOutput())
			{
				maxWaterOutputWidget.setFGColor(0xFF8000);
				maxWaterOutputWidget.setHeadTooltips(//
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.warning").withStyle(TextFormatting.RED), //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.water_will_losing").withStyle(TextFormatting.RED), //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.SATURATING_CONDENSER.getTextComponent()).withStyle(TextFormatting.RED));

			}

			consumer.accept(new ItemStack(MekanismBlocks.STRUCTURAL_GLASS, structuralGlasses));
		}

		@Override
		public void collectResult(Consumer<Widget> consumer)
		{
			super.collectResult(consumer);

			int volume = this.getDimensionVolume();
			int rotors = this.getRotorCount();
			int lowerVolume = this.getLowerVolume(rotors);
			int blades = this.getBladeCount(rotors);
			int vents = this.getVentCount();

			FloatingLong maxProduction = this.getMaxProduction(lowerVolume, blades, vents);
			int maxFlow = MathUtils.clampToInt(this.getMaxFlowRateClamped(lowerVolume, vents));
			long maxWaterOutput = getMaxWaterOutput();
			long steamTank = this.getSteamTank(lowerVolume);
			long energyCapacity = this.getEnergyCapacity(volume);

			FloatingLong productionPerFlow = maxProduction.divide(maxFlow);
			TranslationTextComponent productionPerFlowTooltip = new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.production_per_flow", new TranslationTextComponent("%1$s/%2$s", EnergyDisplay.of(productionPerFlow).getTextComponent(), "mB"));

			ResultWidget maxProductionWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_production"), EnergyDisplay.of(maxProduction).getTextComponent());
			maxProductionWidget.getValueLabel().setTooltips(productionPerFlowTooltip);

			consumer.accept(maxProductionWidget);
			ResultWidget maxFlowRateWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_flow_rate"), VolumeTextHelper.format(maxFlow, VolumeUnit.MILLI, "B/t"));
			consumer.accept(maxFlowRateWidget);

			if (this.needMoreVents)
			{
				LabelWidget valueLabel = maxFlowRateWidget.getValueLabel();
				valueLabel.setFGColor(0xFF8000);
				valueLabel.setTooltips(//
						productionPerFlowTooltip, new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.limited").withStyle(TextFormatting.RED), //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.TURBINE_VENT.getTextComponent()).withStyle(TextFormatting.RED));
			}
			else
			{
				LabelWidget valueLabel = maxFlowRateWidget.getValueLabel();
				valueLabel.setTooltips(productionPerFlowTooltip);
			}

			ResultWidget maxWaterOutputWidget = new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.max_water_output"), VolumeTextHelper.format(maxWaterOutput, VolumeUnit.MILLI, "B/t"));
			consumer.accept(maxWaterOutputWidget);

			if (maxFlow > maxWaterOutput)
			{
				LabelWidget valueLabel = maxWaterOutputWidget.getValueLabel();
				valueLabel.setFGColor(0xFF8000);
				valueLabel.setTooltips(//
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.warning").withStyle(TextFormatting.RED), //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.water_will_losing").withStyle(TextFormatting.RED), //
						new TranslationTextComponent("text.jei_mekanism_multiblocks.tooltip.need_more", GeneratorsBlocks.SATURATING_CONDENSER.getTextComponent()).withStyle(TextFormatting.RED));
			}

			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.steam_tank"), VolumeTextHelper.formatMilliBuckets(steamTank)));
			consumer.accept(new ResultWidget(new TranslationTextComponent("text.jei_mekanism_multiblocks.result.energy_capacity"), EnergyDisplay.of(FloatingLong.create(energyCapacity)).getTextComponent()));
		}

		public long getSteamTank(int lowerVolume)
		{
			return lowerVolume * TurbineMultiblockData.GAS_PER_TANK;
		}

		public long getEnergyCapacity(int volume)
		{
			return volume * 16_000_000L;
		}

		public int getClampedMaxVentCount(int rotorCount)
		{
			int unclamped = this.getNeededVentCountUnclamped(this.getLowerVolume(rotorCount));
			int upperSideBlocks = this.getUpperSideBlocks(rotorCount);
			return Math.min(unclamped, upperSideBlocks);
		}

		public int getClampedMaxCondenserCount(int rotorCount, int ventCount)
		{
			int coils = this.getNeededCoilCount(this.getBladeCount(rotorCount));
			int lowerVolume = this.getLowerVolume(rotorCount);
			double maxFlowRate = this.getMaxFlowRateClamped(lowerVolume, ventCount);
			int unclampedCondensers = MathHelper.ceil(maxFlowRate / MekanismGeneratorsConfig.generators.condenserRate.get());
			return Math.min(unclampedCondensers, this.getUpperInnerVolume(rotorCount) - coils);
		}

		public int getLowerSideBlocks(int rotorCount)
		{
			Vector3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			return innerSquare + (inner.getX() * 2 + inner.getZ() * 2) * rotorCount;
		}

		public int getUpperSideBlocks(int rotorCount)
		{
			Vector3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			int upperHeight = this.getUpperHeight(rotorCount);
			return innerSquare + (inner.getX() * 2 + inner.getZ() * 2) * upperHeight;
		}

		public int getUpperHeight(int rotorCount)
		{
			return this.getDimensionInner().getY() - rotorCount;
		}

		public int getUpperInnerVolume(int rotorCount)
		{
			Vector3i inner = this.getDimensionInner();
			int innerSquare = inner.getX() * inner.getZ();
			int upperHeight = this.getUpperHeight(rotorCount);
			return innerSquare * (upperHeight - 1);
		}

		public int getNeededVentCountUnclamped(int lowerVolume)
		{
			double flowRate = this.getMaxFlowRateUnclamped(lowerVolume);
			return MathHelper.ceil(flowRate / MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
		}

		public int getNeededCoilCount(int bladeCount)
		{
			return MathHelper.ceil((double) bladeCount / MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get());
		}

		public double getMaxFlowRateUnclamped(int lowerVolume)
		{
			return lowerVolume * this.getDisperserCount() * MekanismGeneratorsConfig.generators.turbineDisperserGasFlow.get();
		}

		public double getMaxFlowRateClamped(int lowerVolume, int vents)
		{
			double unclamped = this.getMaxFlowRateUnclamped(lowerVolume);
			return Math.min(unclamped, vents * MekanismGeneratorsConfig.generators.turbineVentGasFlow.get());
		}

		public long getMaxWaterOutput()
		{
			return (long) this.getCondenserCount() * MekanismGeneratorsConfig.generators.condenserRate.get();
		}

		public FloatingLong getMaxProduction(int lowerVolume, int blades, int vents)
		{
			double flowRate = this.getMaxFlowRateClamped(lowerVolume, vents);

			if (flowRate > 0.0D)
			{
				int coils = this.getNeededCoilCount(lowerVolume);
				FloatingLong energyMultiplier = MekanismConfig.general.maxEnergyPerSteam.get().divide(TurbineValidator.MAX_BLADES).multiply(Math.min(blades, coils * MekanismGeneratorsConfig.generators.turbineBladesPerCoil.get()));
				return energyMultiplier.multiply(flowRate);
			}
			else
			{
				return FloatingLong.ZERO;
			}

		}

		public int getLowerVolume(int rotorCount)
		{
			Vector3i outer = this.getDimension();
			return outer.getX() * outer.getZ() * rotorCount;
		}

		public int getDisperserCount()
		{
			Vector3i inner = this.getDimensionInner();
			return (inner.getX() * inner.getZ()) - 1;
		}

		public int getValveCount()
		{
			return this.valvesWidget.getSlider().getIntValue();
		}

		public void setValveCount(int valveCount)
		{
			this.valvesWidget.getSlider().setIntValue(valveCount);
		}

		public int getRotorCount()
		{
			return this.rotorsWidget.getSlider().getIntValue();
		}

		public int getBladeCount(int rotorCount)
		{
			return rotorCount * 2;
		}

		public void setRotorCount(int rotorCount)
		{
			this.rotorsWidget.getSlider().setIntValue(rotorCount);
		}

		public int getCondenserCount()
		{
			return this.condensersWidget.getSlider().getIntValue();
		}

		public void setCondenserCount(int condenserCount)
		{
			this.condensersWidget.getSlider().setIntValue(condenserCount);
		}

		public int getVentCount()
		{
			return this.ventsWidget.getSlider().getIntValue();
		}

		public void setVentCount(int ventCount)
		{
			this.ventsWidget.getSlider().setIntValue(ventCount);
		}

		public boolean isUseStruturalGlass()
		{
			return this.useStructuralGlassCheckBox.isSelected();
		}

		public void setUseStructuralGlass(boolean useStructuralGlass)
		{
			this.useStructuralGlassCheckBox.setSelected(useStructuralGlass);
		}

		@Override
		public int getDimensionWidthMin()
		{
			return 5;
		}

		@Override
		public int getDimensionWidthMax()
		{
			return 17;
		}

		@Override
		public int getDimensionLengthMin()
		{
			return this.getDimensionWidthMin();
		}

		@Override
		public int getDimensionLengthMax()
		{
			return this.getDimensionWidthMax();
		}

		@Override
		public int getDimensionHeightMin()
		{
			return 5;
		}

		@Override
		public int getDimensionHeightMax()
		{
			return 18;
		}

	}

}
