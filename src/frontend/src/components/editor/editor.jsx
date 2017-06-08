import React from 'react'
import { Router, Route, Link } from 'react-router'
import RangeSlider from '../range-slider/range-slider.jsx'
import Config from '../../config.json'

export default class Editor extends React.Component {
	constructor(props) {
		super(props)
		this.state = {
			skillLevel: this.props.skillLvl,
			willLevel: this.props.willLvl
		}
		this.handleSliderChange = this.handleSliderChange.bind(this)
		this.handleAccept = this.handleAccept.bind(this)
	}

	handleSliderChange(val, type) {
		if (type === 'skill') {
			this.setState({
				skillLevel: val
			})
		} else {
			this.setState({
				willLevel: val
			})
		}
	}

	handleAccept() {
		const { skillName } = this.props
		const { skillLevel, willLevel } = this.state
		this.props.handleAccept(skillName, skillLevel, willLevel, 'POST')
		this.props.handleClose()
	}

	render() {
		const { skillLevel, willLevel } = this.state

		return (
			<div className="editor">
				<div className="action-buttons">
					<a className="check" onClick={this.handleAccept}></a>
					<a className="cancel" onClick={this.props.handleClose}></a>
				</div>
				<div className="slider-container">
					<p className="slider-description">Dein Skill-Level</p>
					<RangeSlider
						onSlide={this.handleSliderChange}
						type="skill"
						defaultValue={skillLevel}
						legend={Config.skillLegend} />
					<p className="slider-description">Dein Will-Level</p>
					<RangeSlider
						onSlide={this.handleSliderChange}
						type="will"
						defaultValue={willLevel}
						legend={Config.willLegend} />
				</div>
			</div>
		)
	}
}
