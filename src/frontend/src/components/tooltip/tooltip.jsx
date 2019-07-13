import './tooltip.less'
import React from 'react'
import Icon from '../icon/icon.jsx'

const className = 'tooltip'

class Tooltip extends React.Component {
	constructor(props) {
		super(props)
	}
	render() {
		const { variant } = this.props
		return (
			<div className={`${className} ${className}--${variant}`}>
				<Icon name={`${variant}-tooltip`} size={19} />
				<div className={`${className}-content`}>
					{variant === 'skill' && (
						<div className={`${className}-legend-wrapper`}>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--zero`} />
								</div>
								<span className={`${className}-legend-name`}>head the name</span>
							</div>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--1`} />
								</div>
								<span className={`${className}-legend-name`}>basic</span>
							</div>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--2`} />
								</div>
								<span className={`${className}-legend-name`}>advanced</span>
							</div>
							<div className={`${className}-legend-item`}>
								<div className="level">
									<div className={`skillBar levelBar levelBar--3`} />
								</div>
								<span className={`${className}-legend-name`}>expert</span>
							</div>
						</div>
					)}

					{variant === 'will' && (
						<div className={`${className}-legend-wrapper`}>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>super high</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--3`} />
								</div>
							</div>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>high</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--2`} />
								</div>
							</div>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>ok</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--1`} />
								</div>
							</div>
							<div className={`${className}-legend-item`}>
								<span className={`${className}-legend-name`}>none</span>
								<div className="level">
									<div className={`willBar levelBar levelBar--zero`} />
								</div>
							</div>
						</div>
					)}
				</div>
			</div>
		)
	}
}

export default Tooltip
