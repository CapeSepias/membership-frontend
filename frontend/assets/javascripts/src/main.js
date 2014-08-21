require([
    'lib/bower-components/imager.js/Imager',
    'src/utils/analytics/omniture',
    'src/utils/router',
    'ajax',
    'src/modules/tier/JoinFree',
    'src/modules/tier/JoinPaid',
    'src/modules/tier/Upgrade',
    'src/modules/events/Cta',
    'src/modules/Header',
    'src/modules/events/DatetimeEnhance',
    'src/modules/events/modifyEvent'
], function(
    Imager,
    omnitureAnalytics,
    router,
    ajax,
    JoinFree,
    JoinPaid,
    Upgrade,
    Cta,
    Header,
    DatetimeEnhance,
    modifyEvent
    ) {
    'use strict';

    ajax.init({page: {ajaxUrl: ''}});

    router.match('/event/').to(function () {
        (new DatetimeEnhance()).init();
        (new Cta()).init();
        modifyEvent.init();
    });

    router.match('*/friend/enter-details').to(function () {
        (new JoinFree()).init();
    });

    router.match(['*/payment', '*/partner/enter-details', '*/patron/enter-details']).to(function () {
        (new JoinPaid()).init();
    });

    router.match(['*/tier/change/partner', '*/tier/change/patron']).to(function () {
        (new Upgrade()).init();
    });

    router.match('*').to(function () {
        (new Header()).init();
        omnitureAnalytics.init();

        /* jshint ignore:start */
        // avoid "Do not use 'new' for side effects" error
        new Imager({ availableWidths: [300, 460, 940], availablePixelRatios: [1, 2] });
        /* jshint ignore:end */
    });

    /**
     * We were using domready here but for an unknown reason it is not firing in our production environment.
     * Please ask Ben Chidgey or Chris Finch if there are issues around this.
     */
    router.go();
});
