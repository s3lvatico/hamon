package org.gmnz.hamon.analysis;


import org.gmnz.hamon.integration.GeneralInfoDto;
import org.gmnz.hamon.integration.SectionHitsDto;

import java.util.List;


public interface InfoGathererListener {

	@Deprecated
	void receiveSectionHitsData(List<SectionHitsDto> sectionHitsDtoList);




	@Deprecated
	void receiveGeneralInfoData(GeneralInfoDto generalInfoDto);




	void receiveTrafficStats(List<SectionHitsDto> sectionHitsDtoList, GeneralInfoDto generalInfoDto);

}
