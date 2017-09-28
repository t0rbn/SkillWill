import React from 'react'
import Tooltip from '../tooltip/tooltip'

export class SkillLegendItem extends React.Component {
	constructor(props) {
		super(props)
	}

	render() {
		const { title, wide, handleClickEvent, withTooltip } = this.props

		return (
			<span
				className={`skill-legend__item
				${wide ? 'skill-legend__item--wide' : ''}`}>
				{handleClickEvent ? (
					<span className="skill-legend__button" onClick={handleClickEvent}>
						{title}
					</span>
				) : (
					title
				)}
				{withTooltip && <Tooltip variant={withTooltip} />}
			</span>
		)
	}
}

export class SkillLegend extends React.Component {
	constructor(props) {
		super(props)
	}

	render() {
		const { children } = this.props

		return <div className="skill-legend">{children}</div>
	}
}
