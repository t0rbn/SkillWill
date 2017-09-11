import React from 'react'
import RangeSlider from '../range-slider/range-slider.jsx'
import config from '../../config.json'

export default class Editor extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillLevel: this.props.skillLvl,
			willLevel: this.props.willLvl,
		}
		this.handleSliderChange = this.handleSliderChange.bind(this)
		this.handleAccept = this.handleAccept.bind(this)
	}

	handleSliderChange(val, type) {
		if (type === 'skill') {
			this.setState({
				skillLevel: val,
			})
		} else {
			this.setState({
				willLevel: val,
			})
		}
	}

	handleAccept() {
		const { skillName, isMentor } = this.props
		const { skillLevel, willLevel } = this.state
		this.props.handleAccept(skillName, skillLevel, willLevel, isMentor)
		this.props.handleClose()
	}

	render() {
		const { skillLevel, willLevel } = this.state
		return (
			<div className="editor">
				<div className="action-buttons">
					<a className="check" onClick={this.handleAccept} />
					<a className="cancel" onClick={this.props.handleClose} />
				</div>
				<div className="slider-container">
					<p className="slider-description">Your skill level</p>
					<RangeSlider
						onSlide={this.handleSliderChange}
						type="skill"
						value={skillLevel}
						legend={config.skillLegend}
					/>
					<p className="slider-description">Your will level</p>
					<RangeSlider
						onSlide={this.handleSliderChange}
						type="will"
						value={willLevel}
						legend={config.willLegend}
					/>
				</div>
			</div>
		)
	}
}
