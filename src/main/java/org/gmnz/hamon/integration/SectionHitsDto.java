package org.gmnz.hamon.integration;


/**
 * creato da simone in data 13/03/2018.
 */
public class SectionHitsDto {

	private String section;
	private int hits;



	public SectionHitsDto(String section, int hits) {
		this.section = section;
		this.hits = hits;
	}



	public String getSection() {
		return section;
	}



	public int getHits() {
		return hits;
	}
}
