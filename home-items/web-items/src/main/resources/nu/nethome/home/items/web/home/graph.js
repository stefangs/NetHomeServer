/*
 * Copyright (C) 2005-2013, Stefan Strömberg <stefangs@nethome.nu>
 *
 * This file is part of OpenNetHome  (http://www.nethome.nu)
 *
 * OpenNetHome is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * OpenNetHome is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


$(document).ready(function () {
    $.jqplot.config.enablePlugins = true;
    // Our ajax data renderer which here retrieves a text file.
    // it could contact any source and pull data, however.
    // The options argument isn't used in this renderer.
    var ajaxDataRenderer = function (url, plot, options) {
        var ret = null;
        $.ajax({
            // have to use synchronous here, else the function
            // will return before the data is fetched
            async: false,
            url: url,
            dataType: "json",
            success: function (data) {
                ret = data;
            }
        });
        if (ret.length === 0) {
            $("#chart1").html("No data available for " + graph_title);
        } else {
            $("#chart1").html("");
        }
        return [ret];
    };

// passing in the url string as the jqPlot data argument is a handy
// shortcut for our renderer.  You could also have used the
// "dataRendererOptions" option to pass in the url.
    var plot2 = $.jqplot('chart1', jsonurl, {
        title: graph_title,
        dataRenderer: ajaxDataRenderer,
        dataRendererOptions: {
            unusedOptionalUrl: jsonurl
        },
        seriesDefaults: {
            markerOptions: { show: false }
        },
        axes: {
            xaxis: {
                show: true,
                renderer: $.jqplot.DateAxisRenderer,
                tickOptions: {
                    formatString: tick_format
                }
            }
        },
        cursor: {
            show: true,
            tooltipLocation: 'sw',
            zoom: true
        }
    });
});
