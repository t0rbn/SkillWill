import React from 'react'
import './icon.less'

const Icon = props => {
	const component = 'icon'
	let { size = 24, height, width, name, className } = props
	const classes = [component, `${component}--${name}`, className]
		.join(' ')
		.trim()
	height = height || size
	width = width || size

	return (
		<svg
			className={classes}
			width={width}
			height={height}
			dangerouslySetInnerHTML={{
				__html: `<use xlink:href="#${name}" width="100%" height="100%" fill="currentColor"></use>`,
			}}
		/>
	)
}

export default Icon
