import React from 'react'
import { Link } from 'react-router'
import './navigation.less'

const NavigationItem = props => {
	return (
		<li className="navigation__item">
			<Link className="nav-link" to={props.target}>
				{props.title}
			</Link>
		</li>
	)
}

export default NavigationItem
