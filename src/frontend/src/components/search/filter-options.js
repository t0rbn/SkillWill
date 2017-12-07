function option(value, display) {
  display = display == undefined ? value : display
  return {
    value,
    display
  }
}

const all = option('all')

const hamburg = option('Hamburg')

const frankfurt = option('Frankfurt')

const munich = option('München')

const berlin = option('Berlin')

const prague = option('Prag')

export const companiesFilterOptions = [
  all,
  option('S2 Germany'),
  option('S2 Swipe'),
  option('S2 Commerce'),
  option('S2 Content'),
  option('S2 AG')
]

export const locationOptionsForCompany = {
  'all': [all, hamburg, frankfurt, munich, prague, berlin],
  'S2 Germany': [all, hamburg, frankfurt, munich, prague, berlin],
  'S2 Swipe': [all, hamburg, berlin],
  'S2 Commerce': [all, hamburg, prague],
  'S2 Content': [all, hamburg],
  'S2 AG': [all, hamburg]
}

console.log(companiesFilterOptions, locationOptionsForCompany)