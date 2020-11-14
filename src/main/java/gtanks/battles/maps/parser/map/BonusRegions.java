package gtanks.battles.maps.parser.map;

import gtanks.battles.maps.parser.map.bonus.BonusRegion;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(
    name = "bonus-regions"
)
class BonusRegions {
    private List<BonusRegion> bonusRegions;

    public List<BonusRegion> getBonusRegions() {
        return this.bonusRegions;
    }

    @XmlElement(
        name = "bonus-region"
    )
    public void setBonusRegions(List<BonusRegion> bonusRegions) {
        this.bonusRegions = bonusRegions;
    }
}
