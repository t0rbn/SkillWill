import React from 'react'
import SkillItem from '../skill-item/skill-item'

const TopWills = props => {

	function filterdWills() {
		return props.wills.filter(w => w.willLevel > 1).slice(0, 5)
	}

	if (filterdWills().length < 1) {
		return null;
	}

	return (
		<li className="top-wills skill-listing">
			<div className="listing-header">Top wills</div>
			<ul className="skills-list">
				{filterdWills().map(skill => {
					return <SkillItem skill={skill} key={skill.name} />
				})}
			</ul>
		</li>
	)
}

export default TopWills
