import React from 'react'

export class SkillLegendItem extends React.Component {
	constructor(props) {
		super(props)
	}

	render() {
		const { title, wide, handleClickEvent } = this.props

		return (
			<li
				className={`skill-legend__item
				${wide ? 'skill-legend__item--wide' : ''}`}>
				{handleClickEvent ? (
					<span className="skill-legend__button" onClick={handleClickEvent}>
						{title}
					</span>
				) : (
					title
				)}
			</li>
		)
	}
}

export class SkillLegend extends React.Component {
	constructor(props) {
		super(props)
	}

	render() {
		const { children } = this.props

		return <ul className="skill-legend">{children}</ul>
	}
}
