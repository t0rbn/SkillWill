import React from 'react'
import { Link } from 'react-router'
import './navigation.less'

const NavigationLink = props => {
	return (
		<Link
			className="navigation__link"
			activeClassName={`${props.setActive ? 'navigation__link--active' : ''}`}
			to={props.target}>
			{props.children}
		</Link>
	)
}

export default NavigationLink
