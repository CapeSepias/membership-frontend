/* global YT */

import { loadScript } from 'src/utils/loadScript';
import { raven } from 'src/modules/raven';

const SELECTOR_PLAYER = '.js-video';
const SELECTOR_PLAYER_IFRAME = '.js-video__iframe';
const SELECTOR_PLAYER_OVERLAY = '.js-video__overlay';
const CLASSNAME_IS_PLAYING = 'is-playing';

const playerEls = document.querySelectorAll(SELECTOR_PLAYER);

// Adds a class to an element, backwards compat for IE.
function addClass (elem, newClass) {
    elem.className += ' ' + newClass;
}

// Checks for the presence of a class in an element, IE backwards compat.
function containsClass (elem, className) {
    return elem.className.split(' ').indexOf(className) > -1;
}

/**
 * Nasty UA detection but calling `playVideo` on iOS
 * results in blank player.
 */
function iOSDevice() {
    return /(iPad|iPhone|iPod)/g.test(navigator.userAgent);
}

/**
 * YouTube API is initialised using global callback function
 */
window.onYouTubeIframeAPIReady = function() {
    [].forEach.call(playerEls, function(player){
        var playerIframe = player.querySelector(SELECTOR_PLAYER_IFRAME);
        var playerOverlay = player.querySelector(SELECTOR_PLAYER_OVERLAY);
        var playerApi;

        if(playerIframe && playerOverlay) {

            var autoplay = containsClass(playerIframe, 'autoplay');
            var loop = containsClass(playerIframe, 'loop');

            var events = {
                onReady: function() {
                    playerReady(player, playerApi, playerOverlay, autoplay);
                }
            };

            if (loop) {
                events.onStateChange = function (e) {
                    if (e.data === YT.PlayerState.ENDED) {
                        playerApi.playVideo();
                    }
                }
            }

            playerApi = new YT.Player(playerIframe, { events: events });

        }
    });
};

// Plays the video and hides the overlay.
function playVideo (player, playerApi, playerOverlay) {

    if(!iOSDevice()) {
        try {
            playerApi.playVideo();
        } catch(e) {
            raven.Raven.captureException(e, {tags: { level: 'info' }});
        }
    }

    addClass(player, CLASSNAME_IS_PLAYING);

    setTimeout(function() {
        var parentNode = playerOverlay.parentNode;
        if (parentNode) {
            parentNode.removeChild(playerOverlay);
        }
    }, 2000);

}

// Sets up click-to-play on overlay, or autoplays if applicable.
function playerReady(player, playerApi, playerOverlay, autoplay) {

    playerOverlay.addEventListener('click', function(event) {

        event.preventDefault();
        playVideo(player, playerApi, playerOverlay);

    });

    if (autoplay && !iOSDevice()) {
        playVideo(player, playerApi, playerOverlay, autoplay);
    }

}

export function init() {
    if (playerEls.length) {
        loadScript('//www.youtube.com/iframe_api?noext', {});
    }
}
